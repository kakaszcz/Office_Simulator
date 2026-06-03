package game.model;

import game.core.Simulation;

abstract public class Agent {
    private int x;
    private int y;

    private double visualX;
    private double visualY;

    private String direction = "DOWN"; // "UP", "DOWN", "LEFT", "RIGHT"
    private int animationFrame = 0;    // Indeks klatki (np. 0, 1, 2, 3)
    private int frameTickCounter = 0;  // Spowalniacz animacji nóg

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

    public void updateVisual() {
        // STAŁA PRĘDKOŚĆ
        double speed = 0.04;

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

            // Animacja nóg
            frameTickCounter++;
            if (frameTickCounter >= 8) {
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

    public int getX() { return x; }
    public int getY() { return y; }

    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public String getDirection() { return direction; }
    public int getAnimationFrame() { return animationFrame; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}