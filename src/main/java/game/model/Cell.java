package game.model;

import game.agents.Agent;
import game.core.GameConfiguration; // Import naszej konfiguracji

public class Cell {
    private int x;
    private int y;
    private String type;
    private Agent agent;

    public Cell(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.agent = null;
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

    public boolean isWall() {
        if (type == null) return false;
        return type.toLowerCase().startsWith("wall");
    }

    /**
     * Uniwersalne sprawdzenie dla obu rodzajów biurek na podstawie konfiguracji.
     */
    public boolean isDesk() {
        return GameConfiguration.TILE_TYPE_DESK.equals(type)
                || GameConfiguration.TILE_TYPE_BOSS_DESK.equals(type);
    }

    /**
     * Sprawdza, czy kafelek jest ekspresem do kawy na podstawie konfiguracji.
     */
    public boolean isCoffee() {
        return GameConfiguration.TILE_TYPE_COFFEE.equals(type);
    }
}