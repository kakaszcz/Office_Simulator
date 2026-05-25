public abstract class Worker extends Agent {

    private double efficiency;
    private double experience;
    private int roundsToFinishTask;
    private boolean isOutside;
    private int taskTime;

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

    public boolean shouldBeFired() { return false; }

    public void doTask() {}

    public void goRest() {}

    //public boolean isBossNear() {}
}
