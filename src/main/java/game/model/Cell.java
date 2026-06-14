package game.model;

import game.agents.Agent;
import game.core.GameConfiguration;

/**
 * Reprezentuje pojedynczą komórkę (kafelek) logicznej planszy biura.
 * Przechowuje informacje o współrzędnych geograficznych, typie podłoża,
 * przebywającym na niej agencie oraz o stanie rezerwacji kafelka przez system nawigacji.
 */
public class Cell {
    private int x;
    private int y;
    private String type;
    private Agent agent;

    /** Flaga rezerwacji zapobiegająca jednoczesnemu wybieraniu tego samego punktu docelowego przez wielu agentów. */
    private boolean reserved;

    /**
     * Tworzy nową komórkę planszy o określonych współrzędnych i typie.
     *
     * @param x Współrzędna X komórki na siatce mapy.
     * @param y Współrzędna Y komórki na siatce mapy.
     * @param type Tekstowy identyfikator typu kafelka (np. "floor", "wall", "desk").
     */
    public Cell(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.agent = null;
        this.reserved = false;
    }

    /**
     * Sprawdza, czy na kafelku nie znajduje się obecnie żaden agent.
     *
     * @return true, jeśli komórka jest pusta; false w przeciwnym wypadku.
     */
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

    /**
     * Sprawdza, czy komórka została zablokowana/zarezerwowana soft-lockiem przez zmierzającego do niej agenta.
     *
     * @return true, jeśli kafelek jest zarezerwowany; false, jeśli jest wolny.
     */
    public boolean isReserved() {
        return reserved;
    }

    /**
     * Ustawia stan rezerwacji kafelka biurowego.
     *
     * @param reserved Wartość true w celu zablokowania komórki, false w celu jej zwolnienia.
     */
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    /**
     * Określa, czy po danym kafelku można się fizycznie poruszać (czy jest drożny dla agentów).
     * Komórka jest zdatna do przejścia, jeśli jej typ nie reprezentuje ściany ani przeszkody stałej.
     *
     * @return true, jeśli agent może wejść na ten kafelek; false, jeśli ruch jest zablokowany.
     */
    public boolean isWalkable() {
        return !isWall();
    }

    /**
     * Sprawdza, czy kafelek jest elementem konstrukcyjnym (ścianą) na podstawie jego identyfikatora tekstowego.
     *
     * @return true, jeśli nazwa typu kafelka rozpoczyna się od frazy "wall"; false w przeciwnym wypadku.
     */
    public boolean isWall() {
        if (type == null) return false;
        return type.toLowerCase().startsWith("wall");
    }

    /**
     * Sprawdza, czy komórka pełni funkcję biurka pracowniczego lub stanowiska szefa.
     *
     * @return true, jeśli kafelek pasuje do stałych konfiguracyjnych biurek; false w przeciwnym wypadku.
     */
    public boolean isDesk() {
        return GameConfiguration.TILE_TYPE_DESK.equals(type)
                || GameConfiguration.TILE_TYPE_BOSS_DESK.equals(type);
    }

    /**
     * Sprawdza, czy kafelek reprezentuje automat lub punkt dystrybucji kawy.
     *
     * @return true, jeśli typ komórki pokrywa się z identyfikatorem punktu kawowego.
     */
    public boolean isCoffee() {
        return GameConfiguration.TILE_TYPE_COFFEE.equals(type);
    }
}