import java.util.ArrayList;
import java.util.List;


public class Simulation {
    //public static void main(String[] args) {
    //System.out.println("helool");

    private Board board;
    private List<Agent> agents;

    public Simulation(int numJuniors, int initialBudget) {
        this.board = new Board();
        this.agents = new ArrayList<>();



    /*
    Ta część kodu odpowiada za przydzielanie biurek juniorom, po kolej
    W poprzedniej klasie wyszukuje biurek, a tu przydziela miejsca
     */
        for (int i = 0; i < numJuniors; i++) {
            Cell freeDesk = board.findFirstEmptyCell("desk");
            if (freeDesk != null) {
                junior j = new Junior(board, freeDesk.getX(), freeDesk.getY());
                agents.add(j);
                freeDesk.setAgent(j);
            }
        }
    }
}
