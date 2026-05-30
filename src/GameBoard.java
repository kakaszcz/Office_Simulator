

public class GameBoard {
    private final int width = 11;
    private final int height = 11;
    private Cell[][] grid;
    // NEW: Przechowujemy typy podłóg do renderowania w JavaFX
    private int[][] floorMap;


    //Poprawiony konstruktor
    public GameBoard() {
        createEmptyGrid();
        initalizeBoard();
    }

    private void createEmptyGrid() {
        this.grid = new Cell[height][width];
        this.floorMap = new int[height][width]; // NEW: Inicjalizacja mapy podłóg
    }

    private void initalizeBoard(){

        //Plansza 10x10, wszytsko powinno się zmieścić, edit: jednak 11x11 bedzie symetrycznie
        //---------WARSTWA PODŁÓG ------------
        //0-empty_floor, 3-outdoor(grass), 4- boss_floor
        int[][] layout = {
                {2,2,0,0,0,0,0,4,4,4,4},
                {2,2,0,0,0,0,0,4,4,4,4},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,1,0,1,0,1,0,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,1,0,1,0,1,0,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,1,0,1,0,1,0,1,0,1,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {3,3,3,3,3,3,3,3,3,3,3},
                {3,3,3,3,3,3,3,3,3,3,3},
        };

                for(int y = 0; y <height; y++) {
                    for (int x = 0; x < width; x++) {
                        String type = determineCellType(layout[y][x]);
                        grid[y][x] = new Cell(x, y, type);
                    }
                }
    }


    private String determineCellType(int value){
        if (value == 1) return "desk";
        if (value == 2) return "coffee";
        if (value == 3) return "outdoor";
        if (value == 4) return "boss_office";
        return "floor"; // Domyślnie, jeśli wartość to 0 lub jakakolwiek inna
    }


            //Zabezpieczenie przed wyjsciem poza tablicę
            public boolean isInBounds (int x, int y){
                return x >= 0 && x < width && y >= 0 && y < height;
            }

            /*
            Przykład: Szef w swojej turze wylosuje współrzędne x=5, y=4
             żeby sprawdzić czy ktoś tam stoi
            Przykłąd 2: do umieszczania na danym kafelku agentów
             */
            public Cell getCell(int x, int y) {
                if (!isInBounds(x, y)) return null;
                return grid[y][x];
            }


            /*
            Ta metoda automatycznie pszeszukuje planszę w poszukiwaniu WOLNYCH MIEJSC,
            Jest wywoływana w Simulation class i w zależności od tego co potzrebujemy to tam usatwia
            (Na przykład juniora).
             */
            public Cell findFirstEmptyCell(String type) {
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (grid[y][x].getType().equals(type) && grid[y][x].isEmpty()) {
                            return grid[y][x];
                        }
                    }
                }
                return null;
            }

            /*
            Tutaj dokładnie tak samo jak powyżej, boss na samym pocżatku dostaje info gdzie ma iść
            Bedzie maił swój spawn na pierwszym kafelku w swoim biurze(przynajmniej tak to powinno działać)
             */
            public Cell findBossOfficeCell(){
                for(int y =0; y < height; y++){
                    for(int x = 0; x < width; x++){
                        if(grid[y][x].getType().equals("boss_office") && grid[y][x].isEmpty()){
                            return grid[y][x];
                        }
                    }
                }
                return null;
            }


  //Fizyczna logika ruchu -- agentów

    public boolean placeAgent(Agent agent, int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null && cell.isEmpty()) {
            cell.setAgent(agent);
            return true;
        }
        return false;
    }

    public void removeAgent(int x, int y) {
        Cell cell = getCell(x, y);
        if (cell != null) {
            cell.setAgent(null);
        }
    }

    public boolean moveAgent(int oldX, int oldY, int newX, int newY) {

        Cell oldCell = getCell(oldX, oldY);
        Cell newCell = getCell(newX, newY);

        if (oldCell != null && newCell != null && !oldCell.isEmpty() && newCell.isEmpty()) {
            Agent agent = oldCell.getAgent();
            oldCell.setAgent(null);
            newCell.setAgent(agent);
            return true;
        }
        return false;
    }


            //GETTERY ROZMIARU
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    //dodac placeAgent(); moveAgent(); removeAgent();✔️
    //strasznie duzo rzeczy jest w konstruktorze - moze by zrobic osobne metody zeby byl clean code - void initializeBoard();✔️
    //trzeba bedzie albo dodac pare biurek albo powiekszyc plansze bo przy 10x10 mamy 12 biurek a max 15 workerow✔️
    //zrob biuro szefa bo jest bezdomny! ✔️

/*
   /dobra to tak dodałam mu cały gabinet bo tak będzie łatwiej to w kodzie zapisać imo,
   Nie bedziemy musieli sprawdzać na jakim kafelku stoi w danym momencie,
   TYlko trzeba usatwić że pracownicy np. Juniorzy mają tam zakaz wstępu (To napiszemy jeszzce w klasie junior albo bezpośrednio w nadrzędnej klasie jeśli chcemy ten zakaz rozszerzyć dla seniora i juniora)
*/

