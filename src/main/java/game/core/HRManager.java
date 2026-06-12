package game.core;

import game.agents.Agent;
import game.model.EmployeeRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HRManager {
    // Agent -> Jego Teczka (żeby szybko aktualizować)
    private final Map<Agent, EmployeeRecord> activeRecords = new HashMap<>();

    // Lista zwolnionych
    private final List<EmployeeRecord> firedRecords = new ArrayList<>();

    private int totalHired = 0;

    public void registerHire(Agent agent) {
        activeRecords.put(agent, new EmployeeRecord(agent));
        totalHired++;
    }

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

    public void updateAllRecords() {
        for (Map.Entry<Agent, EmployeeRecord> entry : activeRecords.entrySet()) {
            entry.getValue().updateStats(entry.getKey());
        }
    }

    public List<EmployeeRecord> getActiveRecords() {
        return new ArrayList<>(activeRecords.values());
    }

    public List<EmployeeRecord> getFiredRecords() {
        // Zwracamy kopię listy, aby zabezpieczyć historię przed niepożądaną modyfikacją z zewnątrz
        return new ArrayList<>(firedRecords);
    }

    public int getTotalHired() { return totalHired; }
}