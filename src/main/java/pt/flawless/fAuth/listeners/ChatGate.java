package pt.flawless.fAuth.listeners;

/**
 * Decisão pura sobre o que fazer com uma mensagem de chat.
 *
 * <p>O chat do Spigot é assíncrono (corre fora da thread principal), por isso a decisão
 * recebe apenas booleanos já lidos e nunca toca em estado do servidor. O código 2FA só é
 * comparado quando o jogador está efetivamente à espera dele; um admin que ainda não fez
 * login nunca chega a {@link Decision#CHECK_CODE}, logo não consegue autenticar-se só com
 * o código.</p>
 */
public final class ChatGate {

    public enum Decision { ALLOW, CHECK_CODE, REQUIRE_LOGIN }

    private ChatGate() {
    }

    /**
     * @param loggedIn   o jogador já fez login
     * @param waiting2FA o jogador está à espera de introduzir o código 2FA
     */
    public static Decision decide(boolean loggedIn, boolean waiting2FA) {
        if (waiting2FA) {
            return Decision.CHECK_CODE;
        }
        return loggedIn ? Decision.ALLOW : Decision.REQUIRE_LOGIN;
    }
}
