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

    // ZMIANA: Teraz ta metoda blokuje WSZYSTKIE kafelki, po których nie wolno chodzić!
    public boolean isWall(){
        return "wall".equals(type) ||
                "wall_not_walkable".equals(type) ||
                "floor_not_walkable".equals(type) ||
                "desk".equals(type) ||
                "boss_desk".equals(type);
    }

    // ZMIANA: Uniwersalne sprawdzenie dla obu rodzajów biurek
    public boolean isDesk(){
        return "desk".equals(type) || "boss_desk".equals(type);
    }

    public boolean isCoffee(){
        return "coffee".equals(type);
    }
}