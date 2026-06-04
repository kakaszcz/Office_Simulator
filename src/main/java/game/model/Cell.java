package game.model;

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

    public boolean isEmpty(){
        return this.agent == null;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public Agent getAgent(){
        return agent;
    }

    public void setAgent(Agent agent){
        this.agent = agent;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public boolean isWall() {
        return type.equals("wall")
                || type.equals("wall_not_walkable")
                || type.equals("wall_right")
                || type.equals("wall_back")
                || type.equals("wall_corner")
                || type.equals("wall_sr_corner")  // <-- ZABLOKOWANY NOWY CORNER
                || type.equals("wall_nr_corner")  // <-- ZABLOKOWANY NOWY CORNER
                || type.equals("wall_nl_corner") // <-- ZABLOKOWANY NOWY CORNER
                || type.equals("wall_left"); // <-- BLOKADA DLA NOWEJ LEWEJ ŚCIANY
    }

    // ZMIANA: Uniwersalne sprawdzenie dla obu rodzajów biurek
    public boolean isDesk(){
        return "desk".equals(type) || "boss_desk".equals(type);
    }

    public boolean isCoffee(){
        return "coffee".equals(type);
    }
}