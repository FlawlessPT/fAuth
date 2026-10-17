package pt.flawless.fAuth.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RememberedSessionTest {

    private final RememberedSession session = new RememberedSession("1.2.3.4", 1000);

    @Test
    void sameIpBeforeExpiry_valid() {
        assertTrue(session.isValidFor("1.2.3.4", 999));
    }

    @Test
    void otherIp_invalid() {
        assertFalse(session.isValidFor("5.6.7.8", 999));
    }

    @Test
    void atExpiry_invalid() {
        assertFalse(session.isValidFor("1.2.3.4", 1000));
    }

    @Test
    void afterExpiry_invalid() {
        assertFalse(session.isValidFor("1.2.3.4", 1001));
    }
}
