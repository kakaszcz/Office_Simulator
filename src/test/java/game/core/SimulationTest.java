package game.core;

import game.agents.Agent;
import game.agents.Boss;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa dla głównego silnika symulacji {@link Simulation}.
 * Weryfikuje kluczowe mechanizmy biznesowe: zarządzanie budżetem, upływ czasu,
 * naliczanie kar za błędy krytyczne oraz kalkulacje statystyczne (Success Rate, Efficiency).
 */
class SimulationTest {

    private Simulation simulation;
    private final int initialBudget = 50000;

    @BeforeEach
    void setUp() {
        // Given - Inicjalizujemy nową symulację z przykładowymi wartościami startowymi
        // Tworzymy 2 juniorów, 2 seniorów i budżet początkowy
        simulation = new Simulation(2, 2, initialBudget);
    }

    @Test
    @DisplayName("Inicjalizacja symulacji powinna poprawnie powołać Szefa i ustawić budżet")
    void constructor_shouldInitializeBossAndBudgetCorrectly() {
        // Then - Sprawdzamy czy stan początkowy zgadza się z założeniami konstruktora
        assertNotNull(simulation.getGameBoard(), "Plansza gry powinna zostać zainicjalizowana.");
        assertEquals(initialBudget, simulation.getBudget(), 0.01, "Budżet początkowy powinien być zgodny z parametrem.");

        Boss boss = simulation.getBoss();
        assertNotNull(boss, "Szef powinien zostać automatycznie wygenerowany i dodany do listy agentów.");
        assertTrue(simulation.isRunning(), "Po uruchomieniu symulacja powinna mieć flagę isRunning jako true.");
    }

    @Test
    @DisplayName("Metoda earnMoney powinna poprawnie zwiększać budżet firmy")
    void earnMoney_shouldIncreaseCompanyBudget() {
        // When - Firma zarabia fundusze za ukończone zadanie
        double income = 1500.50;
        simulation.earnMoney(income);

        // Then - Budżet powinien wzrosnąć o zarobioną kwotę
        assertEquals(initialBudget + income, simulation.getBudget(), 0.01, "Budżet nie wzrósł o podaną kwotę zarobku.");
    }

    @Test
    @DisplayName("Formatowanie czasu symulacji powinno prawidłowo przeliczać tury na dni i godziny")
    void getSimulationTimeFormatted_shouldReturnCorrectDayAndHour() {
        // Given - Startujemy od tury 0 (Dzień 1, godz. 09:00)
        assertEquals("Dzień 1, godz. 09:00", simulation.getSimulationTimeFormatted());

        // When - Wykonujemy 5 kroków (zakładamy, że 5 tur to pełen dzień roboczy zgodnie z logiką metody)
        for (int i = 0; i < 5; i++) {
            simulation.step();
        }

        // Then - Czas powinien przeskoczyć na kolejny dzień o godzinie 09:00
        assertEquals("Dzień 2, godz. 09:00", simulation.getSimulationTimeFormatted());
    }

    @Test
    @DisplayName("Współczynnik skuteczności zadań (Success Rate) powinien poprawnie obliczać proporcje sukcesów do porażek")
    void getSuccessRate_shouldCalculateCorrectPercentageOfSuccessfulTasks() {
        // Given - Sytuacja brzegowa: brak zadań na start powinien zwracać 100%
        assertEquals(100.0, simulation.getSuccessRate(), 0.01);

        // When - Rejestrujemy 3 sukcesy i 1 porażkę (razem 4 zadania, sukcesy = 75%)
        simulation.incrementSuccess();
        simulation.incrementSuccess();
        simulation.incrementSuccess();
        simulation.incrementFailed();

        // Then - Skuteczność musi wynosić dokładnie 75.0%
        assertEquals(75.0, simulation.getSuccessRate(), 0.01, "Skuteczność zadań została źle obliczona.");
    }

    @Test
    @DisplayName("Zgłaszanie awarii powinno wywołać Fatal Error i odliczyć karę finansową po osiągnięciu limitu błędów")
    void reportJuniorFail_shouldTriggerFatalErrorAndDeductPenaltyWhenLimitReached() {
        // Given - Pobieramy stan budżetu przed awariami
        double budgetBeforeFails = simulation.getBudget();

        // When - Symulujemy błędy Juniorów aż do osiągnięcia limitu wyznaczającego błąd krytyczny
        for (int i = 0; i < GameConfiguration.MAX_FAILS_LIMIT; i++) {
            simulation.reportJuniorFail();
        }

        // Then - Sprawdzamy czy system zarejestrował błąd krytyczny i pobrał opłatę karną
        assertEquals(1, simulation.getTotalFatalErrors(), "Licznik Fatal Errors powinien wzrosnąć do 1.");
        assertEquals(0, simulation.getTotalFails(), "Licznik bieżących błędów tury powinien się zresetować po kryzysie.");

        double expectedBudget = budgetBeforeFails - GameConfiguration.FATAL_ERROR_PENALTY;
        assertEquals(expectedBudget, simulation.getBudget(), 0.01, "Kara za Fatal Error nie została poprawnie odjęta od budżetu.");
    }

    @Test
    @DisplayName("Metoda repairFail powinna skutecznie zmniejszać licznik bieżących awarii w systemie")
    void repairFail_shouldDecrementCurrentFailsCounter() {
        // Given - Zgłaszamy dwa błędy do systemu
        simulation.reportJuniorFail();
        simulation.reportJuniorFail();
        assertEquals(2, simulation.getTotalFails());

        // When - Senior naprawia jeden z błędów
        simulation.repairFail();

        // Then - Licznik błędów powinien spaść do 1
        assertEquals(1, simulation.getTotalFails(), "Licznik błędów nie został zmniejszony po naprawie.");
    }

    @Test
    @DisplayName("Metoda removeAgent powinna bezpowrotnie usunąć pracownika z listy oraz wyczyścić jego kafelek na planszy")
    void removeAgent_shouldClearAgentFromListAndBoardCell() {
        // Given - Pobieramy pierwszego lepszego agenta z brzegu
        assertFalse(simulation.getAgents().isEmpty(), "Lista agentów nie może być pusta na starcie.");
        Agent targetAgent = simulation.getAgents().get(0);

        // When - Usuwamy agenta z symulacji
        simulation.removeAgent(targetAgent);

        // Then - Agent nie może figurować w strukturach silnika symulacji
        assertFalse(simulation.getAgents().contains(targetAgent), "Agent nadal znajduje się na globalnej liście silnika.");

        // Weryfikacja czy komórka planszy na której stał została wyczyszczona (null)
        var cell = simulation.getGameBoard().getCell(targetAgent.getX(), targetAgent.getY());
        if (cell != null) {
            assertNull(cell.getAgent(), "Komórka planszy po usuniętym agencie nie została wyczyszczona.");
        }
    }

    @Test
    @DisplayName("Średnia wydajność biura (Average Efficiency) nie powinna wywołać błędu dzielenia przez zero, gdy brak pracowników")
    void getAverageEfficiency_shouldReturnZero_whenNoWorkersInSimulation() {
        // Given - Czyścimy wszystkich agentów z symulacji, aby zasymulować puste biuro
        for (Agent agent : new java.util.ArrayList<>(simulation.getAgents())) {
            simulation.removeAgent(agent);
        }
        assertTrue(simulation.getAgents().isEmpty());

        // When - Wywołujemy kalkulację średniej wydajności
        double avgEff = simulation.getAverageOfficeEfficiency();

        // Then - Metoda powinna bezpiecznie zwrócić 0.0 zamiast rzucić ArithmeticException
        assertEquals(0.0, avgEff, 0.01, "Puste biuro powinno skutkować średnią wydajnością równą 0.0%.");
    }
}