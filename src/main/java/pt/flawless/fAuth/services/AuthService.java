package pt.flawless.fAuth.services;

import pt.flawless.fapi.database.bcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Regras de autenticação sobre o {@link AuthRepository}.
 *
 * <p>Nunca bloqueia e nunca lança na thread chamadora: toda a falha (incluindo
 * {@link java.util.concurrent.RejectedExecutionException} de um executor encerrado) chega como futuro
 * falhado. O BCrypt corre apenas no executor do hash, que é separado do pool da base de dados, e o hash
 * é calculado antes de se tocar na base de dados, para não prender uma ligação durante o cálculo.
 */
public class AuthService {

    private static final int BCRYPT_COST = 12;

    private final AuthRepository repository;
    private final Executor hashExecutor;

    public AuthService(AuthRepository repository, Executor hashExecutor) {
        this.repository = repository;
        this.hashExecutor = hashExecutor;
    }

    /** @return se o UUID tem registo */
    public CompletableFuture<Boolean> isRegistered(UUID uuid) {
        try {
            return repository.isRegistered(uuid);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Calcula o hash BCrypt no executor do hash e só depois regista. Falha com
     * {@link pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException} se o UUID já existir.
     */
    public CompletableFuture<Void> register(UUID uuid, String username, String plainPassword) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST)), hashExecutor)
                    .thenCompose(hash -> repository.register(uuid, username, hash));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Procura o hash guardado e só então compara no executor do hash. Utilizador desconhecido dá
     * {@code false} sem calcular nada.
     */
    public CompletableFuture<Boolean> verifyPassword(UUID uuid, String plainPassword) {
        try {
            return repository.findPasswordHash(uuid).thenCompose(stored -> check(stored, plainPassword));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<Boolean> check(Optional<String> stored, String plainPassword) {
        if (!stored.isPresent()) {
            return CompletableFuture.completedFuture(false);
        }
        String hash = stored.get();
        try {
            return CompletableFuture.supplyAsync(() -> BCrypt.checkpw(plainPassword, hash), hashExecutor);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
