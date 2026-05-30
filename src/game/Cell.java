package game;

public class Cell {
    private int x;
    private int y;
    private String type;    //zmienna bedzie przyjmowała wartości: 'desk', 'coffee', 'outdoor', 'floor'
    private Agent agent;    //referencja do agenta

    //Konstruktor - wywoływany przez klasę board
    public Cell(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.agent = null;      //ustawienie flagi początkowej na 0
    }

    //metoda połączona z  findFirstEmptyCell
    //zwraca true jęsli kafelek(game.Cell) jest pusty (nikt na nim nie stoi)
    public boolean isEmpty(){
        return this.agent == null;
    }

    // Gettery i Settery
    // Wyciągają wartości private HERMETYZACJA
    public String getType(){    //
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public Agent getAgent(){
        return agent;
    }

    //Ta metoda będzie przydzielała biurka i je zabierała
    public void setAgent(Agent agent){
        this.agent = agent;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

}
