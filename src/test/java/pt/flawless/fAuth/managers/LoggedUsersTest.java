package pt.flawless.fAuth.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LoggedUsersTest {

    @Test
    void addRemoveAndIsLoggedIn() {
        LoggedUsers users = new LoggedUsers();
        UUID id = UUID.randomUUID();
        assertFalse(users.isLoggedIn(id));
        users.addLoggedUser(id);
        assertTrue(users.isLoggedIn(id));
        users.removeLoggedUser(id);
        assertFalse(users.isLoggedIn(id));
    }

    @Test
    void clearLoggedUsers_emptiesAll() {
        LoggedUsers users = new LoggedUsers();
        users.addLoggedUser(UUID.randomUUID()).addLoggedUser(UUID.randomUUID());
        users.clearLoggedUsers();
        assertTrue(users.getLoggedInPlayers().isEmpty());
    }

    @Test
    void getLoggedInPlayers_isUnmodifiableView() {
        LoggedUsers users = new LoggedUsers();
        Set<UUID> view = users.getLoggedInPlayers();
        assertThrows(UnsupportedOperationException.class, () -> view.add(UUID.randomUUID()));
    }

    @Test
    void getLoggedInPlayers_reflectsLaterChanges() {
        LoggedUsers users = new LoggedUsers();
        Set<UUID> view = users.getLoggedInPlayers();
        UUID id = UUID.randomUUID();
        users.addLoggedUser(id);
        assertTrue(view.contains(id));
    }

    @Test
    @Timeout(30)
    void concurrentAdds_areAllKept() throws Exception {
        final int threads = 8;
        final int perThread = 500;
        LoggedUsers users = new LoggedUsers();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        users.addLoggedUser(UUID.randomUUID());
                    }
                    return null;
                });
            }
            start.countDown();
        } finally {
            pool.shutdown();
        }
        assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
        assertEquals(threads * perThread, users.getLoggedInPlayers().size());
    }
}
