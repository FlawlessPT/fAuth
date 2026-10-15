package pt.flawless.fAuth.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizedUsersTest {

    @BeforeEach
    void clean() {
        AuthorizedUsers.clearWaiting2FA();
    }

    @Test
    void isUserAuthorized_knownAndUnknownAdmin() {
        assertTrue(AuthorizedUsers.isUserAuthorized("FlawlessYasuo"));
        assertFalse(AuthorizedUsers.isUserAuthorized("Outro"));
    }

    @Test
    void matchesCode_correct() {
        assertTrue(AuthorizedUsers.matchesCode("FlawlessYasuo", "1234"));
    }

    @Test
    void matchesCode_wrong() {
        assertFalse(AuthorizedUsers.matchesCode("FlawlessYasuo", "4321"));
    }

    @Test
    void matchesCode_noCodeForName_false() {
        assertFalse(AuthorizedUsers.matchesCode("Outro", "1234"));
    }

    @Test
    void matchesCode_nullMessage_false() {
        assertFalse(AuthorizedUsers.matchesCode("FlawlessYasuo", null));
    }

    @Test
    void matchesCode_isCaseSensitiveToNamesOnly() {
        // O nome do jogador é chave exata; o código atual (1234) não tem letras,
        // por isso a comparação de maiúsculas do código não é testável.
        assertFalse(AuthorizedUsers.matchesCode("flawlessyasuo", "1234"));
    }

    @Test
    void waiting2FA_addRemoveClear() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        AuthorizedUsers.addPlayerToWaitingFor2FA(a);
        AuthorizedUsers.addPlayerToWaitingFor2FA(b);
        assertTrue(AuthorizedUsers.isPlayerWaiting2FA(a));
        AuthorizedUsers.removePlayerFromWaiting2FA(a);
        assertFalse(AuthorizedUsers.isPlayerWaiting2FA(a));
        assertTrue(AuthorizedUsers.isPlayerWaiting2FA(b));
        AuthorizedUsers.clearWaiting2FA();
        assertFalse(AuthorizedUsers.isPlayerWaiting2FA(b));
    }
}
