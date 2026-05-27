public class Boss extends Agent{
    private String name;
    private int control_range;
    private int coffeeTimer;
    private int[][] bossLocation;

    public Boss(String name, int x, int y) {
        super(x, y);
        this.name = name;
        this.control_range = 1;
        this.coffeeTimer = 0;
    }

    @Override
    public void act() {
        // 1. Sprawdzenie coffeeTimer >= 10 -> idź do coffeeTable [1].
        // 2. Porównanie previousBudget z obecnym -> wybór celu w moveTo() [1].
        // 3. Sprawdzenie sąsiadów (Junior -> boost/zwolnienie, Senior -> talking) [1].
    }

    public void moveTo() {}

    public void fireWorker(Worker worker) {}

    // Getter dla imienia szefa, żeby inne klasy mogły z niego korzystać
    public String getName() {
        return name;
    }
}


//Dopisałam zmienną z imieniem, bo dla juniorów losujemy to tutaj możemy mu je nadać
