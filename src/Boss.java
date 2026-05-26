public class Boss extends Agent{
    private int control_range;
    private int coffeeTimer;
    private int[][] bossLocation;

    public Boss(int x, int y) {
        super(x, y);
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
}
