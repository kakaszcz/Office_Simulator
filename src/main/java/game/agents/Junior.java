package game.agents;

import game.core.GameConfiguration;
import game.core.Simulation;

/**
 * Reprezentuje pracownika o poziomie Junior w symulacji.
 * Junior charakteryzuje się wyższą podatnością na błędy wynikającą z niskiego doświadczenia.
 * Klasa implementuje unikalny system trwałego zapisu błędów w "karierze" pracownika
 */
public class Junior extends Worker {
    private int numberOfFails;
    private int lifetimeFails;
    private boolean wasBossNeighborInPreviousTurn = false;

    /**
     * Tworzy nowy obiekt Juniora i inicjalizuje jego statystyki zawodowe.
     * Zeruje początkowe liczniki błędów bieżących oraz całkowitych.
     *
     * @param x Początkowa logiczna współrzędna X na siatce planszy.
     * @param y Początkowa logiczna współrzędna Y na siatce planszy.
     * @param efficiency Bazowa efektywność pracownika (wpływa na zmęczenie).
     * @param experience Poziom doświadczenia (im mniejszy, tym większa szansa na błąd).
     */
    public Junior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.numberOfFails = 0;
        this.lifetimeFails = 0;
    }

    public boolean wasBossNeighborInPreviousTurn() { return wasBossNeighborInPreviousTurn; }
    /**
     * Zmienia flagę informującą, czy w poprzedniej turze Junior znajdował się
     * w bezpośrednim sąsiedztwie Szefa (zasięg kontroli).
     *
     * @param value true, jeśli Szef stał obok; false w przeciwnym wypadku.
     */
    public void setWasBossNeighborInPreviousTurn(boolean value) { this.wasBossNeighborInPreviousTurn = value; }

    /**
     * Dynamicznie oblicza aktualną szansę Juniora na popełnienie błędu w zadaniu.
     * Algorytm łączy bazową szansę wynikającą z braku doświadczenia z karnym
     * modyfikatorem zmęczenia (wyliczanym z aktualnej efektywności). Wynik końcowy
     * jest zabezpieczony dolną granicą zapisaną w konfiguracji gry.
     *
     * @return Wartość typu double reprezentująca procentową szansę na błąd (0.0 - 1.0).
     */
    @Override
    public double getFailChance() {
        // Bazowa szansa wynikająca z braku doświadczenia
        double baseFailChance = 1.0 - this.getExperience();

        // MODYFIKATOR ZMĘCZENIA: Im mniejsza efektywność (bliżej 0), tym większa szansa na błąd
        // Jeśli efficiency spadnie np. do 0.4, (1.0 - 0.4) doda aż 0.6 do szansy na błąd
        double fatiguePenalty = 1.0 - this.getEfficiency();

        double totalFailChance = baseFailChance + (fatiguePenalty * 0.5); // 0.5 to siła wpływu zmęczenia

        // Zwracamy wyliczoną szansę, ale nie mniejszą niż absolutne minimum z konfiguracji
        return Math.max(GameConfiguration.JUNIOR_MIN_FAIL_CHANCE, totalFailChance);
    }

    /**
     * Obsługuje zdarzenie awarii i popełnienia krytycznego błędu przez Juniora.
     * Zwiększa indywidualne statystyki porażek pracownika oraz wysyła globalny
     * raport do głównego silnika symulacji w celu aktualizacji budżetu i nastroju Szefa.
     *
     * @param sim Główny obiekt silnika symulacji (Simulation) rejestrujący błędy w biurze.
     */
    @Override
    public void handleTaskFailure(Simulation sim) {
        this.incrementFails();
        sim.reportJuniorFail();
    }

    /**
     * Weryfikuje, czy pracownik posiada fatalne wyniki i kwalifikuje się do zwolnienia.
     * Sprawdza standardowe kryteria wydajnościowe z klasy bazowej Worker, a dodatkowo
     * weryfikuje, czy całkowity licznik błędów w karierze (lifetimeFails) przekroczył limit 5 awarii.
     *
     * @return true, jeśli pracownik kwalifikuje się do zwolnienia przez HR; false w przeciwnym wypadku.
     */
    @Override
    public boolean hasTerribleMetrics() {
        // HR patrzy na całkowitą historię porażek (lifetimeFails),
        // więc pracownik nie oszuka systemu po globalnym czyszczeniu błędów w biurze
        return super.hasTerribleMetrics() || this.lifetimeFails >= 5;
    }

    public int getTasksFailed() { return this.numberOfFails; }

    public void resetCurrentFails() {
        this.numberOfFails = 0;
    }

    public void incrementFails() {
        this.numberOfFails++;
        this.lifetimeFails++; // Zawsze rośnie, odkłada się w "kartotece" pracownika
        System.out.println(this.getName() + " zawalił zadanie! Indywidualne (obecne): "
                + this.numberOfFails + ", Łącznie w karierze: " + this.lifetimeFails);
    }
}