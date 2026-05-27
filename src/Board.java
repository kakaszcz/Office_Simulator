import java.util.ArrayList;
import java.util.List;

public class Board {
    private int width = 11;
    private int height = 11;
    private Cell[][] grid;

    public Board() {
        this.grid = new Cell[height][width];

        //Plansza 10x10, wszytsko powinno się zmieścić, edit: jednak 11x11 bedzie symetrycznie
        //0-floor
        //1-desk
        //2-coffee
        //3-outdoor
        //4-boss_office
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
                        String type = "floor";
                        if (layout[y][x] == 1) type = "desk";
                        else if (layout[y][x] == 2) type = "coffee";
                        else if (layout[y][x] == 3) type = "outdoor";

                        grid[y][x] = new Cell(x, y, type);
                    }
                }
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
            public Cell finfBossOfficeCell(){
                for(int y =0; y < height; y++){
                    for(int x = 0; x < width; x++){
                        if(grid[y][x].getType().equals("Boss_office") && grid[y][x].isEmpty()){
                            return grid[y][x];
                        }
                    }
                }
                return null;
            }


    public int getWidth() { return width; }
    public int getHeight() { return height; }

    //dodac placeAgent(); moveAgent(); removeAgent();
    //strasznie duzo rzeczy jest w konstruktorze - moze by zrobic osobne metody zeby byl clean code - void initializeBoard();
    //trzeba bedzie albo dodac pare biurek albo powiekszyc plansze bo przy 10x10 mamy 12 biurek a max 15 workerow✔️
    //zrob biuro szefa bo jest bezdomny! ✔️
    /*
   /Dodałam mu cały gabinet bo tak będzie łatwiej to w kodzie zapisać imo,
   Nie bedziemy musieli sprawdzać na jakim kafelku stoi w danym momencie,
   TYlko trzeba usatwić że pracownicy np. Juniorzy mają tm zakaz wstępu

     */
    }

