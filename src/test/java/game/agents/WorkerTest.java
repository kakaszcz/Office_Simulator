package game.agents;

import game.core.GameConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa odpowiedzialna za weryfikację uniwersalnej logiki
 * klas bazowych, zaimplementowanej w abstrakcyjnej klasie Worker.
 * Testuje mechanizm dynamicznego obliczania czasu trwania zadania (computeTaskTime).
 */
class WorkerTest {

    private Worker efficientWorker;
    private Worker inefficientWorker;

    @BeforeEach
    void setUp() {
        // Używamy Seniora
        // Pracownik bardzo wydajny
        efficientWorker = new Senior(0, 0, 1.0, 1.0);

        // Pracownik bardzo niewydajny
        inefficientWorker = new Senior(1, 1, 0.1, 0.1);
    }

    /**
     * Test weryfikuje hipotezę: im wyższa wydajność pracownika,
     * tym mniej tur powinno zająć mu wykonanie taska
     */
    @Test
    void testComputeTaskTimeDependsOnPerformance() {
        // Pobieramy wyliczony czas dla obu typów pracowników
        int shortTime = efficientWorker.computeTaskTime();
        int longTime = inefficientWorker.computeTaskTime();

        // Wydajny pracownik MUSI skończyć zadanie szybciej (w mniej tur) niż niewydajny
        assertTrue(shortTime < longTime,
                "Pracownik o wyższej wydajności powinien wykonać zadanie w krótszym czasie.");
    }

    /**
     * Sprawdzenie war. granicznego (Edge Case).
     * Czas zadania nigdy nie może wynosić 0 ani być liczbą ujemną,
     * minimalny czas w biurze to zawsze co najmniej 1 tura.
     */
    @Test
    void testComputeTaskTimeIsNeverLessThanOne() {
        int time = efficientWorker.computeTaskTime();

        // Sprawdzamy, czy zabezpieczenie Math.max(1, calculatedTime) działa poprawnie
        assertTrue(time >= 1,
                "Wyliczony czas trwania zadania musi wynosić co najmniej 1 turę.");
    }
}