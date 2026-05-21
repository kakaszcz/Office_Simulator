public class Worker extends Agent {

    double efficincy;
    double experience;

    public Worker(int id, int x, int y, double efficincy, double experience) {
        super(id, x, y);
        this.efficincy = efficincy;
        this.experience = experience;
    }

    public double calculatePerformance() {
        double performance;
        return performance = (efficincy + experience)/2.0;
    }
}
