package pt.flawless.fAuth.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException;
import pt.flawless.fAuth.util.AuthExecutors;
import pt.flawless.fapi.database.bcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String PASSWORD = "segredo123";
    private static final Executor DIRECT = Runnable::run;

    /** Hash de {@link #PASSWORD} calculado uma só vez (o custo 12 é lento). */
    private static String storedHash;

    @BeforeAll
    static void computeHash() {
        storedHash = BCrypt.hashpw(PASSWORD, BCrypt.gensalt(12));
    }

    /** Duplo em memória: mapa uuid -> hash, ordem das chamadas e último hash recebido. */
    static class InMemoryAuthRepository extends AuthRepository {
        final Map<UUID, String> hashes = new HashMap<>();
        final List<String> calls = Collections.synchronizedList(new ArrayList<>());
        volatile String lastHashReceived;
        volatile Thread registerThread;
        Optional<RememberedSession> remember = Optional.empty();
        CompletableFuture<Optional<RememberedSession>> rememberFailed;
        UUID setUuid;
        String setIp;
        long setUntil;
        UUID clearedUuid;

        InMemoryAuthRepository() {
            super(null);
        }

        @Override
        public CompletableFuture<Boolean> isRegistered(UUID uuid) {
            calls.add("isRegistered");
            return CompletableFuture.completedFuture(hashes.containsKey(uuid));
        }

        @Override
        public CompletableFuture<Optional<String>> findPasswordHash(UUID uuid) {
            calls.add("findPasswordHash");
            return CompletableFuture.completedFuture(Optional.ofNullable(hashes.get(uuid)));
        }

        @Override
        public CompletableFuture<Optional<RememberedSession>> findRemember(UUID uuid) {
            if (rememberFailed != null) {
                return rememberFailed;
            }
            return CompletableFuture.completedFuture(remember);
        }

        @Override
        public CompletableFuture<Void> setRemember(UUID uuid, String ip, long untilMillis) {
            setUuid = uuid;
            setIp = ip;
            setUntil = untilMillis;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Void> clearRemember(UUID uuid) {
            clearedUuid = uuid;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public synchronized CompletableFuture<Void> register(UUID uuid, String username, String passwordHash) {
            calls.add("register");
            registerThread = Thread.currentThread();
            lastHashReceived = passwordHash;
            if (hashes.containsKey(uuid)) {
                return CompletableFuture.failedFuture(new UserAlreadyRegisteredException(uuid));
            }
            hashes.put(uuid, passwordHash);
            return CompletableFuture.completedFuture(null);
        }
    }

    /** Executor direto que conta as submissões. */
    static class CountingExecutor implements Executor {
        final AtomicInteger count = new AtomicInteger();

        @Override
        public void execute(Runnable command) {
            count.incrementAndGet();
            command.run();
        }
    }

    private static final Executor REJECTING = command -> {
        throw new RejectedExecutionException("fechado");
    };

    @Test
    void register_storesBcryptHash() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID uuid = UUID.randomUUID();

        new AuthService(repo, DIRECT).register(uuid, "Joao", PASSWORD).get(30, TimeUnit.SECONDS);

        String stored = repo.hashes.get(uuid);
        assertNotNull(stored);
        assertTrue(stored.startsWith("$2a$"), stored);
        assertNotEquals(PASSWORD, stored);
        assertTrue(BCrypt.checkpw(PASSWORD, stored));
    }

    @Test
    void register_hashesBeforeTouchingRepository() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();

        new AuthService(repo, DIRECT).register(UUID.randomUUID(), "Joao", PASSWORD).get(30, TimeUnit.SECONDS);

        assertEquals(List.of("register"), repo.calls);
        assertNotNull(repo.lastHashReceived);
        assertNotEquals(PASSWORD, repo.lastHashReceived);
        assertTrue(repo.lastHashReceived.startsWith("$2a$"));
        assertTrue(BCrypt.checkpw(PASSWORD, repo.lastHashReceived));
    }

    @Test
    void register_duplicateFailsWithUserAlreadyRegistered() {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID uuid = UUID.randomUUID();
        repo.hashes.put(uuid, storedHash);

        ExecutionException e = assertThrows(ExecutionException.class,
                () -> new AuthService(repo, DIRECT).register(uuid, "Joao", PASSWORD).get(30, TimeUnit.SECONDS));

        assertInstanceOf(UserAlreadyRegisteredException.class, e.getCause());
    }

    @Test
    void verifyPassword_correctPassword_true() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID uuid = UUID.randomUUID();
        repo.hashes.put(uuid, storedHash);

        assertTrue(new AuthService(repo, DIRECT).verifyPassword(uuid, PASSWORD).get(30, TimeUnit.SECONDS));
    }

    @Test
    void verifyPassword_wrongPassword_false() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID uuid = UUID.randomUUID();
        repo.hashes.put(uuid, storedHash);

        assertFalse(new AuthService(repo, DIRECT).verifyPassword(uuid, "errada").get(30, TimeUnit.SECONDS));
    }

    @Test
    void verifyPassword_unknownUser_false() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        CountingExecutor counting = new CountingExecutor();

        boolean result = new AuthService(repo, counting).verifyPassword(UUID.randomUUID(), PASSWORD)
                .get(30, TimeUnit.SECONDS);

        assertFalse(result);
        assertEquals(List.of("findPasswordHash"), repo.calls);
        assertEquals(0, counting.count.get(), "nenhum hash deve ser calculado para um utilizador desconhecido");
    }

    @Test
    void isRegistered_delegates() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID known = UUID.randomUUID();
        repo.hashes.put(known, storedHash);
        AuthService service = new AuthService(repo, DIRECT);

        assertTrue(service.isRegistered(known).get(5, TimeUnit.SECONDS));
        assertFalse(service.isRegistered(UUID.randomUUID()).get(5, TimeUnit.SECONDS));
        assertEquals(List.of("isRegistered", "isRegistered"), repo.calls);
    }

    @Test
    void register_rejectedExecutor_failsFutureWithoutThrowing() {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        AuthService service = new AuthService(repo, REJECTING);

        CompletableFuture<Void> future = assertDoesNotThrow(
                () -> service.register(UUID.randomUUID(), "Joao", PASSWORD));

        assertTrue(future.isCompletedExceptionally());
        ExecutionException e = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(RejectedExecutionException.class, e.getCause());
        assertTrue(repo.calls.isEmpty(), "o repositório não deve ser tocado");
    }

    @Test
    void verifyPassword_rejectedExecutor_failsFutureWithoutThrowing() {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        UUID uuid = UUID.randomUUID();
        repo.hashes.put(uuid, storedHash);
        AuthService service = new AuthService(repo, REJECTING);

        CompletableFuture<Boolean> future = assertDoesNotThrow(() -> service.verifyPassword(uuid, PASSWORD));

        assertTrue(future.isCompletedExceptionally());
        ExecutionException e = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(RejectedExecutionException.class, e.getCause());
    }

    @Test
    void register_runsOnDaemonFauthHashThread() throws Exception {
        ExecutorService executor = AuthExecutors.newHashExecutor();
        try {
            InMemoryAuthRepository repo = new InMemoryAuthRepository();

            new AuthService(repo, executor).register(UUID.randomUUID(), "Joao", PASSWORD)
                    .get(30, TimeUnit.SECONDS);

            Thread thread = repo.registerThread;
            assertNotNull(thread);
            assertTrue(thread.getName().matches("fauth-hash-\\d+"), thread.getName());
            assertTrue(thread.isDaemon());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void hasValidSession_validSession_true() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        repo.remember = Optional.of(new RememberedSession("1.2.3.4", 2000));
        assertTrue(new AuthService(repo, DIRECT).hasValidSession(UUID.randomUUID(), "1.2.3.4", 1000).get());
    }

    @Test
    void hasValidSession_otherIp_false() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        repo.remember = Optional.of(new RememberedSession("1.2.3.4", 2000));
        assertFalse(new AuthService(repo, DIRECT).hasValidSession(UUID.randomUUID(), "5.6.7.8", 1000).get());
    }

    @Test
    void hasValidSession_expired_false() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        repo.remember = Optional.of(new RememberedSession("1.2.3.4", 2000));
        assertFalse(new AuthService(repo, DIRECT).hasValidSession(UUID.randomUUID(), "1.2.3.4", 2000).get());
    }

    @Test
    void hasValidSession_noSession_false() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        assertFalse(new AuthService(repo, DIRECT).hasValidSession(UUID.randomUUID(), "1.2.3.4", 1000).get());
    }

    @Test
    void hasValidSession_repositoryFails_failedFuture() {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        repo.rememberFailed = CompletableFuture.failedFuture(new IllegalStateException("bd"));
        assertTrue(new AuthService(repo, DIRECT).hasValidSession(UUID.randomUUID(), "1.2.3.4", 1).isCompletedExceptionally());
    }

    @Test
    void enableAndDisableRemember_delegate() throws Exception {
        InMemoryAuthRepository repo = new InMemoryAuthRepository();
        AuthService service = new AuthService(repo, DIRECT);
        UUID id = UUID.randomUUID();
        service.enableRemember(id, "1.2.3.4", 99).get();
        assertEquals(id, repo.setUuid);
        assertEquals("1.2.3.4", repo.setIp);
        assertEquals(99, repo.setUntil);
        service.disableRemember(id).get();
        assertEquals(id, repo.clearedUuid);
    }

    @Test
    void verifyPassword_realExecutor_true() throws Exception {
        ExecutorService executor = AuthExecutors.newHashExecutor();
        try {
            InMemoryAuthRepository repo = new InMemoryAuthRepository();
            UUID uuid = UUID.randomUUID();
            repo.hashes.put(uuid, storedHash);
            AuthService service = new AuthService(repo, executor);

            assertTrue(service.verifyPassword(uuid, PASSWORD).get(30, TimeUnit.SECONDS));
            assertFalse(service.verifyPassword(uuid, "errada").get(30, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }
}
