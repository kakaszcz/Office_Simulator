package game.core;

import game.agents.Agent;
import game.model.EmployeeRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menedżer zasobów ludzkich (HR) odpowiedzialny za prowadzenie kartotek pracowników.
 * Monitoruje statystyki aktywnych agentów, rejestruje procesy zatrudnienia i zwolnienia
 * oraz przechowuje historyczne dane archiwalne (kartoteki zwolnionych pracowników).
 */
public class HRManager {

    /** Mapa powiązań aktywnych agentów z ich indywidualnymi kartotekami pracowniczymi. */
    private final Map<Agent, EmployeeRecord> activeRecords = new HashMap<>();

    /** Archiwalna lista kartotek pracowników, którzy zostali zwolnieni z symulacji. */
    private final List<EmployeeRecord> firedRecords = new ArrayList<>();

    private int totalHired = 0;

    /**
     * Rejestruje zatrudnienie nowego pracownika. Tworzy dla niego nową kartotekę
     * i zwiększa ogólny licznik zatrudnionych.
     *
     * @param agent Obiekt agenta (pracownika) wstępującego do symulacji.
     */
    public void registerHire(Agent agent) {
        activeRecords.put(agent, new EmployeeRecord(agent));
        totalHired++;
    }

    /**
     * Rejestruje proces zwolnienia pracownika. Wyjmuje jego teczkę z rejestru aktywnych,
     * wymusza ostatnią, archiwalną aktualizację jego statystyk (aby zachować ostateczny stan parametrów),
     * flaguje jako nieaktywnego i przenosi kartotekę do bazy osób zwolnionych.
     *
     * @param agent Obiekt agenta, który zostaje usunięty z symulacji.
     */
    public void registerFire(Agent agent) {
        EmployeeRecord record = activeRecords.remove(agent);
        if (record != null) {
            // FIX: Wymuszamy ostatnią, przedśmiertną aktualizację statystyk teczki.
            // Dzięki temu powód zwolnienia (np. ostatni błąd czy spadek wydajności) zapisze się w historii!
            record.updateStats(agent);

            record.isActive = false;
            firedRecords.add(record);
        }
    }

    /**
     * Iteruje po wszystkich aktywnych pracownikach w biurze i synchronizuje dane
     * w ich kartotekach z ich aktualnym stanem fizycznym na planszy.
     */
    public void updateAllRecords() {
        for (Map.Entry<Agent, EmployeeRecord> entry : activeRecords.entrySet()) {
            entry.getValue().updateStats(entry.getKey());
        }
    }

    /**
     * Pobiera listę wszystkich aktualnie prowadzonych teczek aktywnych pracowników.
     *
     * @return Lista (nowa instancja) zawierająca rekordy pracownicze zatrudnionych osób.
     */
    public List<EmployeeRecord> getActiveRecords() {
        return new ArrayList<>(activeRecords.values());
    }

    /**
     * Pobiera historyczną listę rekordów pracowników, którzy zostali zwolnieni.
     * Zwraca bezpieczną kopię listy, chroniąc wewnętrzne archiwum przed modyfikacjami z zewnątrz.
     *
     * @return Lista zawierająca historyczne rekordy zwolnionych osób.
     */
    public List<EmployeeRecord> getFiredRecords() {
        // Zwracamy kopię listy, aby zabezpieczyć historię przed niepożądaną modyfikacją z zewnątrz
        return new ArrayList<>(firedRecords);
    }

    /**
     * Zwraca całkowitą liczbę osób, które kiedykolwiek zostały zatrudnione w firmie.
     *
     * @return Liczba typu int reprezentująca sumaryczną historię zatrudnienia.
     */
    public int getTotalHired() { return totalHired; }
}