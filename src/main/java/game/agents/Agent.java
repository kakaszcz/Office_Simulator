package game.agents;

import game.core.Simulation;
import game.core.GameConfiguration;
import game.model.Cell;
import game.model.GameBoard;

abstract public class Agent {
    private int x;
    private int y;

    private double visualX;
    private double visualY;

    private String direction = "DOWN"; // "UP", "DOWN", "LEFT", "RIGHT"
    private int animationFrame = 0;    // Indeks klatki (np. 0, 1, 2, 3)
    private double frameTickCounter = 0;  // Płynne dodawanie ułamków prędkości (np. przy 0.25x)

    private static int nextId = 1;
    private int id;
    private String name;

    public Agent(int x, int y) {
        this.id = Agent.nextId++;
        this.x = x;
        this.y = y;

        // Na starcie pozycja wizualna to dokładnie ten sam kafelek
        this.visualX = x;
        this.visualY = y;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    // Metoda do obsługi tur (logika)
    public abstract void act(GameBoard board, Simulation sim);

    public void updateVisual(double gameSpeed) {
        double speed = GameConfiguration.AGENT_BASE_VISUAL_SPEED * gameSpeed;

        double diffX = this.x - this.visualX;
        double diffY = this.y - this.visualY;

        // Obliczamy fizyczną odległość do celu
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);

        if (distance > 0.01) {
            // JESTEŚMY W RUCHU
            if (distance <= speed) {
                // Krok jest tak mały, że dotarliśmy do celu
                this.visualX = this.x;
                this.visualY = this.y;
            } else {
                // Przesuwamy się liniowo w stronę celu
                this.visualX += (diffX / distance) * speed;
                this.visualY += (diffY / distance) * speed;
            }

            // Ustalenie kierunku dla rysowania
            if (Math.abs(diffX) > Math.abs(diffY)) {
                this.direction = (diffX > 0) ? "RIGHT" : "LEFT";
            } else {
                this.direction = (diffY > 0) ? "DOWN" : "UP";
            }

            frameTickCounter += gameSpeed;
            if (frameTickCounter >= GameConfiguration.AGENT_ANIMATION_FRAME_DELAY) {
                animationFrame = (animationFrame + 1) % 4;
                frameTickCounter = 0;
            }
        } else {
            // STOIMY W MIEJSCU
            this.visualX = this.x;
            this.visualY = this.y;
            this.animationFrame = 0;
            this.frameTickCounter = 0;
        }
    }

    // Warunek dla pętli w MainApp
    public boolean isCurrentlyWalking() {
        return Math.abs(this.x - this.visualX) > 0.01 || Math.abs(this.y - this.visualY) > 0.01;
    }

    protected void moveRandomly(GameBoard board, boolean allowDesks) {
        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < directions.length; i++) indices.add(i);
        java.util.Collections.shuffle(indices);

        for (int index : indices) {
            int nextX = getX() + directions[index][0];
            int nextY = getY() + directions[index][1];

            if (board.isInBounds(nextX, nextY)) {
                Cell cell = board.getCell(nextX, nextY);

                // Sprawdzamy, czy kafelek fizycznie istnieje, nie jest ścianą i nikt na nim nie stoi
                if (cell != null && !cell.isWall() && cell.getAgent() == null) {

                    // Jeśli to NIE JEST Szef (allowDesks == false), zabraniamy mu wchodzić na biurka
                    if (!allowDesks && "desk".equals(cell.getType())) {
                        continue; // Skipujemy ten kafelek, szukamy innego kierunku
                    }

                    // Skoro wszystko jest bezpieczne, wykonujemy JEDEN ruch na planszy
                    if (board.moveAgent(getX(), getY(), nextX, nextY)) {
                        setX(nextX); // Aktualizujemy wewnętrzny stan agenta
                        setY(nextY);
                        return; // Ruch udany, przerywamy pętlę i kończymy turę
                    }
                }
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public String getDirection() { return direction; }
    public int getAnimationFrame() { return animationFrame; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}