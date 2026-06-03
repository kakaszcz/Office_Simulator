package game.model;

import game.core.Simulation;

abstract public class Agent {
    private int x;
    private int y;

    private double visualX;
    private double visualY;

    private static int nextId = 1;
    private int id;
    private String name;

    public Agent(int x, int y) {
        this.id = Agent.nextId++;
        this.x = x;
        this.y = y;

        // Na starcie pozycja wizualna pokrywa się z kafelkiem
        this.visualX = x;
        this.visualY = y;
    }

    public void setName (String name) { this.name = name; }

    public String getName() { return name; }

    // Metoda do obsługi tur (logika)
    public abstract void act(GameBoard board, Simulation sim);

    // NOWA METODA: Wywoływana w każdej klatce grafiki (60 razy na sekundę).
    // Sprawia, że pozycja ekranowa płynnie "goni" pozycję kafelka (x, y).
    public void updateVisual() {
        double lerpFactor = 0.15; // 0.05 to wolny spacer, 0.25 to szybki bieg
        this.visualX += (this.x - this.visualX) * lerpFactor;
        this.visualY += (this.y - this.visualY) * lerpFactor;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // gettery dla silnika graficznego
    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }

    // Settery dla zmiany pozycji logicznej
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}