

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
        //-------  WARSTWA PODŁÓG  ------------
        //0-empty_floor, 1-outdoor(grass), 2- boss_floor
        int[][] floorLayout = {
                {0,0,0,0,0,0,0,2,2,2,2},
                {0,0,0,0,0,0,0,2,2,2,2},
                {0,0,0,0,0,0,0,2,2,2,2},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,1,1,1,1,1,1,1}, // Trawa na dole
                {1,1,1,1,1,1,1,1,1,1,1}
        };

        // ----- WARSTWA OBIEKTÓW (0 = pusto, 6 = ściana, 7 = kawa, 9 = 15 biurek) ---
        int[][] objectLayout = {
                {7,7,0,0,0,0,0,6,6,6,6}, // Aneks i górna ściana szefa
                {7,7,0,0,0,0,0,6,0,0,0}, // Ściana boczna szefa
                {0,0,0,0,0,0,0,6,0,0,0}, // Wejście do szefa (puste 0 na podłodze 2)
                {0,9,0,9,0,9,0,9,0,9,0}, // Rząd 1: 5 biurek
                {0,0,0,0,0,0,0,0,0,0,0}, // Korytarz do chodzenia
                {0,9,0,9,0,9,0,9,0,9,0}, // Rząd 2: 5 biurek
                {0,0,0,0,0,0,0,0,0,0,0}, // Korytarz do chodzenia
                {0,9,0,9,0,9,0,9,0,9,0}, // Rząd 3: 5 biurek
                {0,6,6,6,6,6,6,6,6,6,6}, // Ściana odgradzająca ogród (pierwsze pole '0' to drzwi na trawę!)
                {0,0,0,0,0,0,0,0,0,0,0}, // Przestrzeń przed wyjściem
                {0,0,0,0,0,0,0,0,0,0,0}  // Czysta trawa w ogródku
        };

                this.floorMap = floorLayout; // Zapis mapy podłóg dla renderera JavaFX


                for(int y = 0; y <height; y++) {
                    for (int x = 0; x < width; x++) {
                        String type = determineCellType(objectLayout[y][x], floorLayout[y][x]);
                        grid[y][x] = new Cell(x, y, type);
                    }
                }
    }


    // POPRAWIONE: Metoda przyjmuje teraz obiekt i podłogę oraz korzysta z GameConfiguration
    private String determineCellType(int objectValue, int floorValue) {
        if (objectValue == GameConfiguration.OBJ_DESK) return "desk";
        if (objectValue == GameConfiguration.OBJ_COFFEE) return "coffee";
        if (objectValue == GameConfiguration.OBJ_WALL) return "wall";

        if (floorValue == GameConfiguration.FLOOR_BOSS_OFFICE) return "boss_office";
        if (floorValue == GameConfiguration.FLOOR_OUTDOOR) return "outdoor";

        return "floor";
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
        if (cell != null && cell.isEmpty() && !cell.getType().equals("wall")) {
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

        if (oldCell != null && newCell != null && !oldCell.isEmpty() && newCell.isEmpty() && !newCell.getType().equals("wall")) {
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


    public int[][] getFloorMap() {return floorMap;}
}

//dodac placeAgent(); moveAgent(); removeAgent();✔️
    //strasznie duzo rzeczy jest w konstruktorze - moze by zrobic osobne metody zeby byl clean code - void initializeBoard();✔️
    //trzeba bedzie albo dodac pare biurek albo powiekszyc plansze bo przy 10x10 mamy 12 biurek a max 15 workerow✔️
    //zrob biuro szefa bo jest bezdomny! ✔️

/*
   /dobra to tak dodałam mu cały gabinet bo tak będzie łatwiej to w kodzie zapisać imo,
   Nie bedziemy musieli sprawdzać na jakim kafelku stoi w danym momencie,
   TYlko trzeba usatwić że pracownicy np. Juniorzy mają tam zakaz wstępu (To napiszemy jeszzce w klasie junior albo bezpośrednio w nadrzędnej klasie jeśli chcemy ten zakaz rozszerzyć dla seniora i juniora)
*/

