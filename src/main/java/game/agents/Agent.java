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
    private double frameTickCounter = 0;  // Płynne dodawanie ułamków prędkości

    private static int nextId = 1;
    private int id;
    private String name;


    // Kolejka kroków do wykonania przez animację wizualną
    private java.util.Queue<int[]> visualPath = new java.util.LinkedList<>();

    public void addWaypoint(int wx, int wy) {
        visualPath.add(new int[]{wx, wy});
    }

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

    /**
     * Metoda ta odpowiada za płynne przemieszczanie obrazka agenta na ekranie (renderowanie ruchu).
     * Metoda oblicza dystans do kolejnego punktu na mapie przy użyciu Twierdzenia Pitagorasa.
     * Jeżeli agent jest w ruchu, automatycznie wyznacza kierunek jego patrzenia (UP, DOWN,
     * LEFT, RIGHT) oraz zarządza pętlą klatek animacji chodu (to jeszcze nie zosatło dodane)
     * W przypadku dotarcia do celu, resetuje klatki animacji do pozycji stojącej.
     *
     * @param gameSpeed Współczynnik prędkości symulacji przekazywany z suwaka w menu głównym.
     */
    public void updateVisual(double gameSpeed) {
        // Mnożenie bazowej prędkości przez wartość z suwaka prędkości gry
        double speed = GameConfiguration.AGENT_BASE_VISUAL_SPEED * gameSpeed;

        // Domyślnie celem jest aktualna pozycja logiczna
        double targetX = this.x;
        double targetY = this.y;

        // Jeśli w pamięci animacji są kroki pośrednie, bierzemy pierwszy z nich
        if (!visualPath.isEmpty()) {
            targetX = visualPath.peek()[0];
            targetY = visualPath.peek()[1];
        }

        double diffX = targetX - this.visualX;
        double diffY = targetY - this.visualY;
        double distance = Math.sqrt(diffX * diffX + diffY * diffY); // Pitagoras

        if (distance > 0.01) {
            if (distance <= speed) {
                // Jesteśmy na punkcie pośrednim
                this.visualX = targetX;
                this.visualY = targetY;
                if (!visualPath.isEmpty()) visualPath.poll(); // Usuwamy zaliczony punkt z kolejki
            } else {
                // Idziemy w kierunku celu
                this.visualX += (diffX / distance) * speed;
                this.visualY += (diffY / distance) * speed;
            }

            // Ustalanie kierunku patrzenia postaci (dominujący kierunek ruchu)
            if (Math.abs(diffX) > Math.abs(diffY)) {
                this.direction = (diffX > 0) ? "RIGHT" : "LEFT";
            } else {
                this.direction = (diffY > 0) ? "DOWN" : "UP";
            }

            // ODKOMENTOWANE I ZOPTYMALIZOWANE: Animacja przebierania nogami podczas ruchu
            this.frameTickCounter += gameSpeed;
            if (this.frameTickCounter >= GameConfiguration.AGENT_ANIMATION_FRAME_DELAY) {
                this.animationFrame = (this.animationFrame + 1) % 4; // Cykl klatek 0,1,2,3
                this.frameTickCounter = 0;
            }
        } else {
            // Dotarliśmy do ostatecznego celu tury
            this.visualX = targetX;
            this.visualY = targetY;
            if (!visualPath.isEmpty()) {
                visualPath.poll();
            } else {
                // Ludzik stoi w miejscu - resetujemy klatkę do domyślnej
                this.animationFrame = 0;
                this.frameTickCounter = 0;
            }
        }
    }

    // Warunek sprawdzający w pętli renderowania, czy agent jeszcze się przemieszcza wizualnie
    public boolean isCurrentlyWalking() {
        return Math.abs(this.x - this.visualX) > 0.01 || Math.abs(this.y - this.visualY) > 0.01 || !visualPath.isEmpty();
    }

    /**
     * Odpowiada za wykonanie losowego ruchu agenta o jeden kafelek w promieniu 8 sąsiednich pól.
     * Metoda losowo miesza dostępne kierunki (pionowo, poziomo oraz na ukos), sprawdza granice
     * planszy, obecność ścian oraz innych postaci. Pozwala również zablokować wchodzenie na biurka
     * pracownicze w zależności od przekazanej flagi konfiguracji.
     *
     * @param board Obiekt planszy (GameBoard), na której odbywa się ruch i sprawdzane są kafelki.
     * @param allowDesks Flaga określająca, czy agent ma pozwolenie na wchodzenie na kafelki typu biurko ("desk").
     */
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

                if (cell != null && !cell.isWall() && cell.getAgent() == null) {
                    if (!allowDesks && "desk".equals(cell.getType())) {
                        continue;
                    }

                    if (board.moveAgent(getX(), getY(), nextX, nextY)) {
                        // REFAKTOR: setX i setY automatycznie dodadzą punkt do pamięci animacji
                        setX(nextX);
                        setY(nextY);
                        return;
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

    // Settery automatycznie rejestrują ścieżkę wizualną, zabezpieczając płynność animacji kafelkowej
    public void setX(int x) {
        this.x = x;
        addWaypoint(this.x, this.y);
    }

    public void setY(int y) {
        this.y = y;
        addWaypoint(this.x, this.y);
    }
}