package pt.flawless.fAuth.util;

import org.junit.jupiter.api.Test;
import pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class FailuresTest {

    @Test
    void unwrap_completionException() {
        IllegalStateException cause = new IllegalStateException("x");
        assertSame(cause, Failures.unwrap(new CompletionException(cause)));
    }

    @Test
    void unwrap_nestedExecutionAndCompletion() {
        IllegalStateException cause = new IllegalStateException("x");
        Throwable nested = new CompletionException(new ExecutionException(new CompletionException(cause)));
        assertSame(cause, Failures.unwrap(nested));
    }

    @Test
    void unwrap_plainExceptionIsItself() {
        RuntimeException e = new RuntimeException("plain");
        assertSame(e, Failures.unwrap(e));
        assertNull(Failures.unwrap(null));
    }

    @Test
    void unwrap_userAlreadyRegisteredThroughFuture() {
        UUID uuid = UUID.randomUUID();
        CompletableFuture<Void> f = CompletableFuture.failedFuture(new UserAlreadyRegisteredException(uuid));
        CompletionException ce = assertThrows(CompletionException.class, f::join);
        Throwable real = Failures.unwrap(ce);
        assertInstanceOf(UserAlreadyRegisteredException.class, real);
        assertEquals("utilizador já registado: " + uuid, real.getMessage());
    }
}
