package pt.flawless.fAuth.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.flawless.fAuth.TestDatabaseService;
import pt.flawless.fAuth.TestDb;
import pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException;
import pt.flawless.fapi.database.bcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRepositoryIT {

    private static final long TIMEOUT_S = 30;
    private static final String HASH_A = "$2a$12$" + "a".repeat(53);
    private static final String HASH_B = "$2a$12$" + "b".repeat(53);

    private TestDatabaseService db;
    private AuthRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        db = TestDb.service();
        TestDb.resetSchema(db);
        repo = new AuthRepository(db);
    }

    @AfterEach
    void tearDown() {
        if (db != null) {
            db.close();
        }
    }

    private static <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(TIMEOUT_S, TimeUnit.SECONDS);
    }

    private static Throwable failureOf(CompletableFuture<?> future) {
        ExecutionException e = assertThrows(ExecutionException.class, () -> future.get(TIMEOUT_S, TimeUnit.SECONDS));
        return e.getCause();
    }

    @Test
    void register_thenIsRegisteredAndFindHash() throws Exception {
        UUID id = UUID.randomUUID();
        await(repo.register(id, "Alice", HASH_A));
        assertTrue(await(repo.isRegistered(id)));
        assertEquals(Optional.of(HASH_A), await(repo.findPasswordHash(id)));
    }

    @Test
    void isRegistered_unknownUuid_false() throws Exception {
        assertFalse(await(repo.isRegistered(UUID.randomUUID())));
    }

    @Test
    void findPasswordHash_unknownUuid_empty() throws Exception {
        assertEquals(Optional.empty(), await(repo.findPasswordHash(UUID.randomUUID())));
    }

    @Test
    void register_duplicate_failsWithUserAlreadyRegistered() throws Exception {
        UUID id = UUID.randomUUID();
        await(repo.register(id, "Alice", HASH_A));
        Throwable cause = failureOf(repo.register(id, "Alice", HASH_B));
        assertInstanceOf(UserAlreadyRegisteredException.class, cause);
        assertEquals(Optional.of(HASH_A), await(repo.findPasswordHash(id)));
    }

    @Test
    void register_concurrentDuplicates_exactlyOneSucceeds() throws Exception {
        final int n = 8;
        UUID id = UUID.randomUUID();
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            hashes.add("$2a$12$" + String.valueOf((char) ('a' + i)).repeat(53));
        }
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            CountDownLatch ready = new CountDownLatch(n);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<CompletableFuture<Void>>> submitted = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                String hash = hashes.get(i);
                submitted.add(pool.submit(() -> {
                    ready.countDown();
                    assertTrue(start.await(TIMEOUT_S, TimeUnit.SECONDS));
                    return repo.register(id, "Racer", hash);
                }));
            }
            assertTrue(ready.await(TIMEOUT_S, TimeUnit.SECONDS));
            start.countDown();
            int successes = 0;
            int duplicates = 0;
            for (Future<CompletableFuture<Void>> f : submitted) {
                CompletableFuture<Void> result = f.get(TIMEOUT_S, TimeUnit.SECONDS);
                try {
                    result.get(TIMEOUT_S, TimeUnit.SECONDS);
                    successes++;
                } catch (ExecutionException e) {
                    assertInstanceOf(UserAlreadyRegisteredException.class, e.getCause());
                    duplicates++;
                }
            }
            assertEquals(1, successes);
            assertEquals(n - 1, duplicates);
            Optional<String> stored = await(repo.findPasswordHash(id));
            assertTrue(stored.isPresent());
            assertTrue(hashes.contains(stored.get()));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void register_nameTooLong_failsAsSqlException() {
        Throwable cause = failureOf(repo.register(UUID.randomUUID(), "x".repeat(17), HASH_A));
        assertInstanceOf(SQLException.class, cause);
        assertFalse(cause instanceof UserAlreadyRegisteredException);
    }

    @Test
    void setRemember_thenFindRemember() throws Exception {
        UUID id = UUID.randomUUID();
        await(repo.register(id, "Alice", HASH_A));
        await(repo.setRemember(id, "1.2.3.4", 1_700_000_000_000L));
        assertEquals(Optional.of(new RememberedSession("1.2.3.4", 1_700_000_000_000L)), await(repo.findRemember(id)));
    }

    @Test
    void clearRemember_findRememberEmpty() throws Exception {
        UUID id = UUID.randomUUID();
        await(repo.register(id, "Alice", HASH_A));
        await(repo.setRemember(id, "1.2.3.4", 1_700_000_000_000L));
        await(repo.clearRemember(id));
        assertEquals(Optional.empty(), await(repo.findRemember(id)));
    }

    @Test
    void findRemember_unknownUuid_empty() throws Exception {
        assertEquals(Optional.empty(), await(repo.findRemember(UUID.randomUUID())));
    }

    @Test
    void registeredHashFitsColumn() throws Exception {
        UUID id = UUID.randomUUID();
        String hash = BCrypt.hashpw("segredo", BCrypt.gensalt(12));
        assertEquals(60, hash.length());
        await(repo.register(id, "Alice", hash));
        Optional<String> stored = await(repo.findPasswordHash(id));
        assertNotNull(stored);
        assertEquals(Optional.of(hash), stored);
    }
}
