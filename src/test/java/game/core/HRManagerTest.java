package game.core;

import game.agents.Agent;
import game.model.EmployeeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy {@link HRManager}.
 * Weryfikują poprawność cyklu życia pracownika w systemie kadrowym (zatrudnienie, aktualizacja danych, zwolnienie)
 * oraz odporność struktur danych na manipulacje z zewnątrz.
 */
class HRManagerTest {

    private HRManager hrManager;
    private Agent dummyAgent;

    @BeforeEach
    void setUp() {
        // Inicjalizacja menedżera przed każdym testem
        hrManager = new HRManager();

        // Tworzenie instancji anonimowej klasy Agent z implementacją metody abstrakcyjnej
        dummyAgent = new Agent(0, 0) {
            @Override
            public void act(game.model.GameBoard board, game.core.Simulation simulation) {
                // Metoda celowo pusta - HRManager testuje tylko kartoteki,
                // nie potrzebujemy symulować zachowania agenta na planszy.
            }
        };
    }

    @Test
    @DisplayName("Powinien poprawnie zarejestrować zatrudnienie i zwiększyć globalny licznik")
    void registerHire_shouldAddAgentToActiveRecordsAndIncrementTotalHired() {
        // Given - stan początkowy z setUp

        // When - rejestrujemy zatrudnienie agenta
        hrManager.registerHire(dummyAgent);

        // Then - sprawdzamy czy trafił do kartoteki i podbił licznik
        List<EmployeeRecord> active = hrManager.getActiveRecords();

        assertEquals(1, active.size(), "Lista aktywnych powinna zawierać dokładnie jednego pracownika.");
        assertEquals(1, hrManager.getTotalHired(), "Globalny licznik zatrudnionych (totalHired) powinien wynosić 1.");
    }

    @Test
    @DisplayName("Powinien przenieść kartotekę do archiwum i ustawić status nieaktywny podczas zwolnienia")
    void registerFire_shouldMoveAgentFromActiveToFiredAndMarkAsInactive() {
        // Given - pracownik jest najpierw zatrudniony
        hrManager.registerHire(dummyAgent);
        assertFalse(hrManager.getActiveRecords().isEmpty());

        // When - zwalniamy pracownika
        hrManager.registerFire(dummyAgent);

        // Then - sprawdzamy czy zniknął z aktywnych i trafił do archiwum jako nieaktywny
        assertTrue(hrManager.getActiveRecords().isEmpty(), "Lista aktywnych pracowników powinna być pusta po zwolnieniu.");

        List<EmployeeRecord> fired = hrManager.getFiredRecords();
        assertEquals(1, fired.size(), "Archiwum zwolnionych powinno zawierać dokładnie jeden rekord.");
        assertFalse(fired.get(0).isActive, "Kartoteka zwolnionego pracownika musi mieć flagę isActive ustawioną na false.");
    }

    @Test
    @DisplayName("Nie powinien wykonywać żadnych operacji, jeśli próbujemy zwolnić niezatrudnionego agenta")
    void registerFire_shouldDoNothing_whenAgentWasNotHired() {
        // Given - dummyAgent nie został zarejestrowany w systemie HR

        // When - próbujemy go zwolnić
        hrManager.registerFire(dummyAgent);

        // Then - obie listy powinny pozostać puste, system nie rzuca NullPointerException
        assertTrue(hrManager.getActiveRecords().isEmpty(), "Lista aktywnych powinna być pusta.");
        assertTrue(hrManager.getFiredRecords().isEmpty(), "Lista zwolnionych powinna być pusta.");
    }

    @Test
    @DisplayName("Powinien zsynchronizować dane wszystkich aktywnych rekordów ze stanem agentów")
    void updateAllRecords_shouldInvokeStatsUpdateOnAllActiveAgents() {
        // Given - zatrudniamy pracownika
        hrManager.registerHire(dummyAgent);

        // When & Then - metoda powinna przejść bezbłędnie przez pętlę mapy aktywnych rekordów
        assertDoesNotThrow(() -> hrManager.updateAllRecords(),
                "Metoda updateAllRecords nie powinna generować wyjątków podczas synchronizacji.");
    }

    @Test
    @DisplayName("Modyfikacja pobranej listy zwolnionych nie powinna wpływać na wewnętrzne archiwum HR (Kopia Defensywna)")
    void getFiredRecords_shouldReturnDefensiveCopyProtectingInternalState() {
        // Given - zatrudniamy i zwalniamy pracownika, a następnie pobieramy listę
        hrManager.registerHire(dummyAgent);
        hrManager.registerFire(dummyAgent);
        List<EmployeeRecord> externalFiredList = hrManager.getFiredRecords();

        // When - zewnętrzny kod próbuje zmanipulować / wyczyścić otrzymaną listę
        externalFiredList.clear();

        // Then - wewnętrzne archiwum managera musi pozostać bezpieczne i nienaruszone
        List<EmployeeRecord> internalFiredList = hrManager.getFiredRecords();
        assertEquals(1, internalFiredList.size(),
                "Wewnętrzna lista zwolnionych w HRManagerze została zmodyfikowana! Brak poprawnej kopii defensywnej.");
    }
}