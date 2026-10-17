package pt.flawless.fAuth.services;

import pt.flawless.fAuth.exceptions.UserAlreadyRegisteredException;
import pt.flawless.fapi.database.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Acesso aos utilizadores registados na tabela {@code fauth_users}.
 *
 * <p>Todos os métodos são assíncronos: correm no pool do {@link DatabaseService} e devolvem um futuro que
 * completa excepcionalmente com a excepção original ({@link SQLException} ou
 * {@link UserAlreadyRegisteredException}). Nunca se deve esperar ({@code join()}/{@code get()}) por outro futuro
 * de base de dados dentro de uma tarefa do pool.
 *
 * <p>A classe não é {@code final} e o construtor não valida o argumento: os testes do serviço passam
 * {@code null} num duplo que sobrepõe todos os métodos.
 */
public class AuthRepository {

    /** Código de erro do MySQL para chave duplicada (ER_DUP_ENTRY, SQLState 23000). */
    private static final int DUPLICATE_ENTRY = 1062;

    private static final String IS_REGISTERED = "SELECT 1 FROM fauth_users WHERE uuid = ? LIMIT 1";
    private static final String FIND_HASH = "SELECT password_hash FROM fauth_users WHERE uuid = ?";
    private static final String REGISTER =
            "INSERT INTO fauth_users (uuid, username, password_hash) VALUES (?, ?, ?)";

    private static final String SET_REMEMBER =
            "UPDATE fauth_users SET remember_ip = ?, remember_until = ? WHERE uuid = ?";
    private static final String CLEAR_REMEMBER =
            "UPDATE fauth_users SET remember_ip = NULL, remember_until = NULL WHERE uuid = ?";
    private static final String FIND_REMEMBER =
            "SELECT remember_ip, remember_until FROM fauth_users WHERE uuid = ?";

    private final DatabaseService database;

    public AuthRepository(DatabaseService database) {
        this.database = database;
    }

    /**
     * @return se o UUID tem registo; falha com {@link SQLException} em erro de base de dados
     */
    public CompletableFuture<Boolean> isRegistered(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(IS_REGISTERED)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    /**
     * @return o hash guardado, ou vazio se o UUID não estiver registado; falha com {@link SQLException}
     */
    public CompletableFuture<Optional<String>> findPasswordHash(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(FIND_HASH)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getString(1)) : Optional.<String>empty();
                }
            }
        });
    }

    /** Guarda a sessão lembrada (IP e prazo em epoch ms); falha com {@link SQLException} em erro de BD. */
    public CompletableFuture<Void> setRemember(UUID uuid, String ip, long untilMillis) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SET_REMEMBER)) {
                ps.setString(1, ip);
                ps.setLong(2, untilMillis);
                ps.setString(3, uuid.toString());
                ps.executeUpdate();
            }
        });
    }

    /** Apaga a sessão lembrada. */
    public CompletableFuture<Void> clearRemember(UUID uuid) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(CLEAR_REMEMBER)) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
        });
    }

    /**
     * @return a sessão lembrada, ou vazio se o UUID não existe ou as colunas são NULL; falha com
     * {@link SQLException}
     */
    public CompletableFuture<Optional<RememberedSession>> findRemember(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(FIND_REMEMBER)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.<RememberedSession>empty();
                    }
                    String ip = rs.getString(1);
                    long until = rs.getLong(2);
                    return ip == null || rs.wasNull()
                            ? Optional.<RememberedSession>empty()
                            : Optional.of(new RememberedSession(ip, until));
                }
            }
        });
    }

    /**
     * Regista o utilizador. É a chave primária que decide: não há leitura prévia, por isso dois registos
     * concorrentes do mesmo UUID resultam em exactamente um sucesso.
     *
     * @return futuro que falha com {@link UserAlreadyRegisteredException} se o UUID já existir (erro 1062) e
     * com {@link SQLException} em qualquer outro erro (por exemplo nome com mais de 16 caracteres)
     */
    public CompletableFuture<Void> register(UUID uuid, String username, String passwordHash) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(REGISTER)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, username);
                ps.setString(3, passwordHash);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (e.getErrorCode() == DUPLICATE_ENTRY) {
                    throw new UserAlreadyRegisteredException(uuid);
                }
                throw e;
            }
        });
    }
}
