import java.util.ArrayList;
import java.util.List;

public class Board {
    private int width;
    private int height;
    private Cell[][] grid;

    public Board(int numJuniors, int numSeniors) {
        int totalWorkers = numJuniors + numSeniors;


        int size = (int) Math.ceil(Math.sqrt(totalWorkers * 3));


        if(size <6){
            size = 6;
        }

        this.width = width;     //POlE tej konkretnej klasy, bez this byłaby przypisana wartość parametru podana w funkcji
        this.height = height;
        this.grid = new Cell[width][height];

        initOfficeLayout(totalWorkers);
        }
    }

