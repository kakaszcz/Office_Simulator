import java.util.ArrayList;
import java.util.List;


public class Simulation {
    //public static void main(String[] args) {
    //System.out.println("helool");

    private Board board;
    private List<Agent> agents;
    private int stepCount = 0;
    private boolean isRunning = true;

    public Simulation(int numJuniors, int numSeniors) {
        this.agents = new ArrayList<>();

        //Obliczenia dotyczące tego jak duża mapka powinna się
        //wczytać w zależności od tego jaką ilość agntów wybrał użytkownik,
        //min. size boardu - 6x6

        int totalWorkers = numJuniors * numSeniors;
        int rozmiar = (int) Math.ceil(Math.sqrt(totalWorkers * 3));

       int(rozmiar < 6) rozmiar = 6;

        this.board = new Board(rozmiar, rozmiar, totalWorkers);
    }
}
