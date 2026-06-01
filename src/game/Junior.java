package game;

public class Junior extends Worker{
    private int numberOfFails;
    private static int fails;
    private double failChance;

    public Junior(int x, int y, double efficiency, double experience) {
        super(x, y, efficiency, experience);
        this.numberOfFails = 0;
    }


    public boolean shouldBeFired() {
        // game.Junior zostaje oznaczony do zwolnienia, gdy:
        // - liczba jego błędów (numberOfFails) wyniesie 5 lub więcej [
        // - jego ogólny performance spadnie poniżej 45%.
        // - przyłapanie na dworze
        return this.numberOfFails >= 5 || this.getPerformance() < 0.45;
    }

    @Override
    public void act(GameBoard board) {
        //interakcja z szefem
        //crying
        //konsekwencje przy outside
    }

    //fatal error
    //nowy pracownik
}
