package pt.flawless.fAuth.exceptions;

import java.util.UUID;

/**
 * Lançada quando se tenta registar um UUID que já existe (chave primária duplicada).
 */
public class UserAlreadyRegisteredException extends RuntimeException {

    public UserAlreadyRegisteredException(UUID uuid) {
        super("utilizador já registado: " + uuid);
    }
}
