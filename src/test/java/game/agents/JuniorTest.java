package game.agents;

import game.core.GameConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JuniorTest {

    @Test
    void getFailChance_shouldBeHigherWhenJuniorIsTired() {
        Junior restedJunior = new Junior(0, 0, 1.0, 0.5);
        Junior tiredJunior = new Junior(0, 0, 0.2, 0.5);

        double restedFailChance = restedJunior.getFailChance();
        double tiredFailChance = tiredJunior.getFailChance();

        assertTrue(tiredFailChance > restedFailChance);
        assertTrue(restedFailChance >= GameConfiguration.JUNIOR_MIN_FAIL_CHANCE);
    }

    @Test
    void resetCurrentFails_shouldResetOnlyCurrentFails() {
        Junior junior = new Junior(0, 0, 1.0, 1.0);

        for (int i = 0; i < 5; i++) {
            junior.incrementFails();
        }

        assertEquals(5, junior.getTasksFailed());
        assertTrue(junior.hasTerribleMetrics());

        junior.resetCurrentFails();

        assertEquals(0, junior.getTasksFailed());
        assertTrue(junior.hasTerribleMetrics());
    }

    @Test
    void bossNeighborFlag_shouldBeStoredCorrectly() {
        Junior junior = new Junior(0, 0, 0.8, 0.5);

        assertFalse(junior.wasBossNeighborInPreviousTurn());

        junior.setWasBossNeighborInPreviousTurn(true);

        assertTrue(junior.wasBossNeighborInPreviousTurn());
    }

    @Test
    void recordBossBoost_shouldIncreaseBossBoostCounter() {
        Junior junior = new Junior(0, 0, 0.8, 0.5);

        assertEquals(0, junior.getBossBoosts());

        junior.recordBossBoost();
        junior.recordBossBoost();

        assertEquals(2, junior.getBossBoosts());
    }
}