package pt.flawless.fAuth.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ChatGateTest {

    @Test
    void adminNotLoggedInNotWaiting_requiresLogin_neverChecksCode() {
        ChatGate.Decision d = ChatGate.decide(false, false);
        assertEquals(ChatGate.Decision.REQUIRE_LOGIN, d);
        assertNotEquals(ChatGate.Decision.CHECK_CODE, d);
    }

    @Test
    void notLoggedInNotWaiting_requiresLogin() {
        assertEquals(ChatGate.Decision.REQUIRE_LOGIN, ChatGate.decide(false, false));
    }

    @Test
    void waiting2FA_checksCode() {
        assertEquals(ChatGate.Decision.CHECK_CODE, ChatGate.decide(false, true));
    }

    @Test
    void loggedInNotWaiting_allows() {
        assertEquals(ChatGate.Decision.ALLOW, ChatGate.decide(true, false));
    }

    @Test
    void loggedInAndWaiting_checksCode() {
        assertEquals(ChatGate.Decision.CHECK_CODE, ChatGate.decide(true, true));
    }
}
