package pt.flawless.fAuth.util;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Utilitários para falhas de {@link java.util.concurrent.CompletableFuture}.
 */
public final class Failures {

    private Failures() {
    }

    /**
     * Descasca {@link CompletionException} e {@link ExecutionException} (repetidamente,
     * enquanto tiverem causa) e devolve a causa real. {@code null} devolve {@code null}.
     */
    public static Throwable unwrap(Throwable t) {
        Throwable current = t;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
