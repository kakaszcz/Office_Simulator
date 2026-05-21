abstract public class Agent {
    int x;
    int y;
    static int nextId = 1;
    int id;

    public Agent(int id, int x, int y) {
        this.id = nextId++;
        this.x = x;
        this.y = y;
    }
}
