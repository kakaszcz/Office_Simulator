package game;

public abstract class Worker extends Agent {

    private double efficiency;
    private double experience;
    protected WorkerState state = WorkerState.WAITING;
    private int taskTime = 0;
    private int restTurns = 0;
    private int cryingTurns = 0;
    private int repairingTurns = 0;
    private int talkingTurns = 0;
    private int roundsToFinishTask;
    private boolean isOutside;
    private boolean atCoffeeTable = false;
    private boolean shouldBeFired = false;
    private boolean hasTask = false;
    private int coffeesDrunk = 0;

    public Worker(int x, int y, double efficiency, double experience) {
        super(x, y);
        this.efficiency = efficiency;
        this.experience = experience;
        this.isOutside = false;
    }

    public double getPerformance() {
        double performance;
        return performance = (efficiency + experience)/2.0;
    }

    //BEDZIE WYWOLYWANE PRZEZ KLASE SYMULACJA!!!!!!!! (co 3 tury lub co ile ustalimy ze jest przydielany task)
    public void assignTask() {
        if (state == WorkerState.WAITING) {
            hasTask = true;
        }
    }

    public boolean isShouldBeFired() { return shouldBeFired; }
    public void markFired() {this.shouldBeFired = true; }

    public void doTask() {}

    public void goRest() {}

//======== ile zajmie mu task(obliczanie)
    protected int computeTaskTime() {
        return Math.max(1, (int) Math.round(1 + 4.0 / (1.0 + getPerformance())));
    }

    //public boolean isBossNear() {}
}
