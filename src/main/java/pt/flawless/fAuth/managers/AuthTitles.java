package pt.flawless.fAuth.managers;

import org.bukkit.entity.Player;
import pt.flawless.fapi.title.FTitle;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Títulos de autenticação mostrados a cada jogador. O estado é por jogador (nunca partilhado).
 * Todos os métodos devem ser chamados na thread principal.
 */
public class AuthTitles {

    public static final AuthTitles titles = new AuthTitles();

    private final Map<UUID, FTitle> active = new ConcurrentHashMap<>();

    public void showLogin(Player player) {
        show(player, "§cPara prosseguir, efetua login: /login [password]§c ");
    }

    public void showRegister(Player player) {
        show(player, "§cPara prosseguir, regista-te: /register [password] [password]§c ");
    }

    public void showTwoFactor(Player player) {
        FTitle title = active.get(player.getUniqueId());
        if (title == null) {
            title = new FTitle().setPlayer(player).setTime(999);
            active.put(player.getUniqueId(), title);
        }
        title.clear();
        title.setMessage("§eAdmin detetado. Por favor insere o teu código 2FA:");
        title.send();
    }

    public void clear(Player player) {
        FTitle title = active.remove(player.getUniqueId());
        if (title != null) {
            title.clear();
        }
    }

    /** Esquece o título do jogador sem enviar nada (jogador saiu). */
    public void forget(UUID uuid) {
        active.remove(uuid);
    }

    public void clearAll() {
        active.clear();
    }

    private void show(Player player, String message) {
        FTitle title = new FTitle().setPlayer(player).setTime(999);
        title.setMessage(message);
        active.put(player.getUniqueId(), title);
        title.send();
    }
}
