public class Worker extends Agent {

    double efficiency;
    double experience;

    public Worker(int x, int y, double efficincy, double experience) {
        super(x, y);
        this.efficiency = efficincy;
        this.experience = experience;
    }

    public double calculatePerformance() {
        double performance;
        return performance = (efficiency + experience)/2.0;
    }
}
