package pt.flawless.fAuth.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fábrica do executor dedicado ao cálculo de hashes BCrypt, separado do pool da base de dados.
 */
public final class AuthExecutors {

    private static final int HASH_THREADS = 2;

    private AuthExecutors() {
    }

    /**
     * Cria um executor com 2 threads daemon chamadas {@code fauth-hash-N} (N a partir de 1). Quem o cria é
     * responsável por o encerrar ({@code shutdown}) no {@code onDisable}.
     */
    public static ExecutorService newHashExecutor() {
        AtomicInteger counter = new AtomicInteger();
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "fauth-hash-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(HASH_THREADS, factory);
    }
}
