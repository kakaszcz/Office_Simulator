abstract public class Agent {
    private int x;
    private int y;
    private static int nextId = 1;
    private int id;
    private String name;

    public Agent(int x, int y) {
        this.id = Agent.nextId++;
        this.x = x;
        this.y = y;
    }

    public void setName (String name) { this.name = name; }

    //metoda do obslugi tur
    public abstract void act();

    public int getX() { return x; }
    public int getY() { return y; }

    //settery dla zmiany pozycji
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}
