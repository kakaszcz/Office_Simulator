package game.agents;

import game.core.GameConfiguration;

/**
 * Reprezentuje pracownika o poziomie Senior w symulacji.
 * Senior charakteryzuje się podwyższoną i bardziej stabilną efektywnością pracy.
 * Klasa rozszerza bazową logikę pracownika poprzez automatyczne dodawanie
 * stałego bonusu doświadczenia do ogólnych wyników wydajności.
 */
public class Senior extends Worker {

    private double experienceBonus;

    /**
     * Tworzy nowy obiekt Seniora i inicjalizuje jego pozycję oraz bazowe statystyki.
     * Pobiera unikalny bonus doświadczenia dedykowany dla Seniorów z globalnej konfiguracji gry.
     *
     * @param x Początkowa logiczna współrzędna X na siatce planszy.
     * @param y Początkowa logiczna współrzędna Y na siatce planszy.
     * @param efficiency Bazowa efektywność przypisana pracownikowi.
     * @param experience Poziom doświadczenia zawodowego.
     */
    public Senior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.experienceBonus = GameConfiguration.SENIOR_EXPERIENCE_BONUS;
    }

    /**
     * Oblicza końcową wydajność pracy Seniora w danej turze symulacji.
     * Metoda pobiera standardowy wynik z klasy bazowej Worker, a następnie zwiększa
     * go o bonus stażowy Seniora. Zastosowany algorytm gwarantuje, że ostateczna
     * wydajność pracownika nigdy nie przekroczy maksymalnego limitu wynoszącego 1.0 (100%).
     *
     * @return Wartość typu double reprezentująca końcową wydajność w przedziale (0.0 - 1.0).
     */
    @Override
    public double getPerformance() {
        // Pobieramy standardową wydajność z klasy Worker i dodajemy bonus Seniora.
        // Używamy Math.min, aby ogólna wydajność nigdy nie przekroczyła 1.0 (100%).
        return Math.min(1.0, super.getPerformance() + this.experienceBonus);
    }
}