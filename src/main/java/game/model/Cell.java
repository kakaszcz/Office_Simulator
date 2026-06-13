package game.model;

import game.agents.Agent;
import game.core.GameConfiguration;

public class Cell {
    private int x;
    private int y;
    private String type;
    private Agent agent;

    // REFAKTOR: Flaga miękkiej rezerwacji zapobiegająca podkradaniu kafelków
    private boolean reserved;

    public Cell(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.agent = null;
        this.reserved = false;
    }

    public boolean isEmpty() {
        return this.agent == null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // REFAKTOR: Gettery i settery dla systemu rezerwacji
    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isWall() {
        if (type == null) return false;
        return type.toLowerCase().startsWith("wall");
    }

    public boolean isDesk() {
        return GameConfiguration.TILE_TYPE_DESK.equals(type)
                || GameConfiguration.TILE_TYPE_BOSS_DESK.equals(type);
    }

    public boolean isCoffee() {
        return GameConfiguration.TILE_TYPE_COFFEE.equals(type);
    }
}