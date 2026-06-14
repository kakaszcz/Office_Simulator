package game.agents;

import game.core.GameConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa dla agenta {@link Junior}.
 * Weryfikuje mechaniki specyficzne dla ról juniorskich, takie jak: wpływ zmęczenia
 * na szansę popełnienia błędu, resetowanie liczników oraz reakcję na bliskość Szefa (Boss Boost).
 */
class JuniorTest {

    /**
     * Weryfikuje, czy prawdopodobieństwo porażki (fail chance) rośnie,
     * gdy poziom energii/wypoczęcia Juniora drastycznie spada.
     */
    @Test
    @DisplayName("Szansa na błąd powinna być wyższa, gdy Junior jest zmęczony")
    void getFailChance_shouldBeHigherWhenJuniorIsTired() {
        // Given - Tworzymy dwa skrajne przypadki: Juniora wypoczętego oraz skrajnie zmęczonego
        Junior restedJunior = new Junior(0, 0, 1.0, 0.5);
        Junior tiredJunior = new Junior(0, 0, 0.2, 0.5);

        // When - Pobieramy obliczoną przez algorytm szansę na popełnienie błędu
        double restedFailChance = restedJunior.getFailChance();
        double tiredFailChance = tiredJunior.getFailChance();

        // Then - Zmęczony musi mieć większą szansę na błąd. Sprawdzamy też dolny limit z konfiguracji globalnej.
        assertTrue(tiredFailChance > restedFailChance);
        assertTrue(restedFailChance >= GameConfiguration.JUNIOR_MIN_FAIL_CHANCE);
    }

    /**
     * Sprawdza mechanizm czyszczenia liczników bieżącej tury.
     * Wyczyszczenie bieżących błędów nie powinno wpływać na ogólną, historyczną ocenę wydajności.
     */
    @Test
    @DisplayName("Resetowanie bieżących porażek powinno czyścić tylko licznik tury, zachowując metryki historyczne")
    void resetCurrentFails_shouldResetOnlyCurrentFails() {
        // Given - Tworzymy Juniora i symulujemy serię 5 błędów z rzędu
        Junior junior = new Junior(0, 0, 1.0, 1.0);

        for (int i = 0; i < 5; i++) {
            junior.incrementFails();
        }

        // Weryfikacja stanu wejściowego: błędy zostały nabite, a metryki pracownika są złe
        assertEquals(5, junior.getTasksFailed());
        assertTrue(junior.hasTerribleMetrics());

        // When - Menedżer lub system resetuje bieżący licznik błędów po turze
        junior.resetCurrentFails();

        // Then - Licznik błędów spada do zera, ale negatywna historia w metrykach pozostaje nienaruszona
        assertEquals(0, junior.getTasksFailed());
        assertTrue(junior.hasTerribleMetrics());
    }

    /**
     * Weryfikuje system flagowania obecności Szefa w bezpośrednim sąsiedztwie agenta.
     */
    @Test
    @DisplayName("Flaga sąsiedztwa Szefa powinna być poprawnie zapisywana i odczytywana")
    void bossNeighborFlag_shouldBeStoredCorrectly() {
        // Given - Nowo utworzony Junior domyślnie nie stał obok szefa w poprzedniej turze
        Junior junior = new Junior(0, 0, 0.8, 0.5);

        assertFalse(junior.wasBossNeighborInPreviousTurn());

        // When - Oznaczamy, że Szef pojawił się w sąsiedniej komórce mapy
        junior.setWasBossNeighborInPreviousTurn(true);

        // Then - Flaga stanu musi poprawnie zwrócić wartość true
        assertTrue(junior.wasBossNeighborInPreviousTurn());
    }

    /**
     * Testuje licznik motywacyjnych "kopniaków" (Boss Boosts), które Junior otrzymał od Szefa.
     */
    @Test
    @DisplayName("Zarejestrowanie motywacji od Szefa powinno inkrementować dedykowany licznik")
    void recordBossBoost_shouldIncreaseBossBoostCounter() {
        // Given - Junior startujący z czystym kontem bez żadnych premii motywacyjnych
        Junior junior = new Junior(0, 0, 0.8, 0.5);

        assertEquals(0, junior.getBossBoosts());

        // When - Szef oddziałuje na Juniora dwukrotnie
        junior.recordBossBoost();
        junior.recordBossBoost();

        // Then - Licznik boostów musi wynosić dokładnie 2
        assertEquals(2, junior.getBossBoosts());
    }
}