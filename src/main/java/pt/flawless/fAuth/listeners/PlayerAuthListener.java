package pt.flawless.fAuth.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import pt.flawless.fAuth.services.AuthService;
import pt.flawless.fAuth.util.MainThread;
import pt.flawless.fAuth.util.PlayerIp;
import pt.flawless.fapi.events.PlayerAuthEvent.PlayerAuthEvent;

public class PlayerAuthListener implements Listener {

    private final AuthService service;
    private final Plugin plugin;

    public PlayerAuthListener(AuthService service, Plugin plugin) {
        this.service = service;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAuth(PlayerAuthEvent e) {
        Player player = e.getPlayer();
        FileConfiguration config = plugin.getConfig();
        for (String line : config.getStringList("Mensagens.BemVindo")) {
            player.sendMessage(line.replace("{player}", player.getName()).replace("&", "§"));
        }

        if (!config.getBoolean("Sessao.Ativa", true)) {
            return;
        }
        int days = config.getInt("Sessao.Dias", 7);
        // Em erro de BD não mostra nada: o aviso é só uma dica.
        service.hasValidSession(player.getUniqueId(), PlayerIp.of(player), System.currentTimeMillis())
                .whenComplete((valid, error) -> {
                    if (error != null || valid) {
                        return;
                    }
                    MainThread.run(plugin, () -> {
                        if (MainThread.isSameSession(player)) {
                            player.sendMessage("§eQueres manter a sessão iniciada durante " + days
                                    + " dias? Usa §f/manterlogin§e.");
                        }
                    });
                });
    }
}
