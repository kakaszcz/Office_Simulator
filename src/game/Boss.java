package game;

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
    public void act(GameBoard board) {
        int currentX = getX();
        int currentY = getY();

        int newX = currentX + (Math.random() > 0.5 ? 1 : (Math.random() > 0.5 ? -1 : 0));
        int newY = currentY + (Math.random() > 0.5 ? 1 : (Math.random() > 0.5 ? -1 : 0));

        if(board.moveAgent(currentX, currentY, newX, newY)) {
            setX(newX);
            setY(newY);
            System.out.println(this.name + " moved to " + newX + ", " + newY);
        }


        // 1. Sprawdzenie coffeeTimer >= 10 -> idź do coffeeTable [1].
        // 2. Porównanie previousBudget z obecnym -> wybór celu w moveTo() [1].
        // 3. Sprawdzenie sąsiadów (game.Junior -> boost/zwolnienie, game.Senior -> talking) [1].
    }

    public void moveTo() {}

    public void fireWorker(Worker worker) {}

    // Getter dla imienia szefa, żeby inne klasy mogły z niego korzystać
    public String getName() {
        return name;
    }
}


//Dopisałam zmienną z imieniem, bo dla juniorów losujemy to tutaj możemy mu je nadać
