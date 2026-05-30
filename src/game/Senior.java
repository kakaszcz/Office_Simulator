package game;

public class Senior extends Worker {

    private double experienceBonus;

    public Senior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.experienceBonus = 0.15; //przyklad
    }

    @Override
    public void act() {
        // 1. Sprawdzenie bliskości szefa -> stan 'talking' [3, 4]
        // Jeśli sąsiad == game.Boss? -> efficiency -= 10% i plotkuje 1 turę.

        // 2. Priorytet naprawy błędów -> stan 'repairing' [3]
        // Jeśli ogólne fails >= 1? -> wywołaj repairFail().

        // 3. Sprawdzenie zmęczenia -> stan 'rest' [3]
        // Jeśli efficiency <= 45%? -> wybierz kawę (70%) lub dwór (100%).

        // 4. Standardowa praca -> stan 'working' [3]
        // Jeśli brak błędów i wydajność OK -> wykonuj taska.
    }

    public void repairFail() {
        // Logika z diagramu stanów:
        // - fails-- (zmniejszenie ogólnej liczby błędów juniorów)
        // - efficiency -= 10%
        // - repairTime = taskTime (czas naprawy zależny od performance)
    }
}
