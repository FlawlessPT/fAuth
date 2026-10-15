package pt.flawless.fAuth.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;

/**
 * Devolve trabalho à thread principal do servidor.
 *
 * <p>Os callbacks de base de dados e o chat correm fora da thread principal; o estado do
 * Bukkit (jogadores, eventos, inventários) só pode ser tocado na principal, por isso todo o
 * código que o precise passa por aqui.</p>
 */
public final class MainThread {

    private MainThread() {
    }

    /**
     * Agenda {@code task} na thread principal. Se o plugin já estiver desativado o
     * agendamento falha com {@link RuntimeException}: é registado e a tarefa é descartada.
     */
    public static void run(Plugin plugin, Runnable task) {
        try {
            Bukkit.getScheduler().runTask(plugin, task);
        } catch (RuntimeException e) {
            FConsoleLogger.sendConsoleMessage(
                    "não foi possível agendar tarefa na thread principal: " + e.getMessage(),
                    EMessageTypeEnum.ERROR);
        }
    }
}
