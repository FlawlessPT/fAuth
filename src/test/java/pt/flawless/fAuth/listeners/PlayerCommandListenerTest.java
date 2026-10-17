package pt.flawless.fAuth.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerCommandListenerTest {

    @Test
    void commandName_normalizesCaseSlashAndOwnNamespace() {
        assertEquals("login", PlayerCommandListener.commandName("/Login pass"));
        assertEquals("login", PlayerCommandListener.commandName("  /fauth:login pass"));
        assertEquals("clearchat", PlayerCommandListener.commandName("/clearchat"));
    }

    @Test
    void commandName_otherNamespaceNeverMatchesAllowed() {
        assertEquals("evil:login", PlayerCommandListener.commandName("/evil:login x"));
    }
}
