package game.agents;

import game.core.Simulation;
import game.core.GameConfiguration;
import game.model.*;

//spr.
public class Boss extends Agent {

    private int controlRange;
    private int coffeeTimer;
    private double previousBudget;
    private int madTurnsRemaining = 0;

    /**
     * Tworzy nowy obiekt Szefa i inicjalizuje jego parametry startowe.
     * Ustawia domyślny zasięg kontroli nad pracownikami oraz zapamiętuje
     * początkowy budżet firmy do celów analizy finansowej w kolejnych turach.
     *
     * @param name Imię lub identyfikator Szefa.
     * @param x Początkowa logiczna współrzędna X gabinetu szefa(na planszy).
     * @param y Początkowa logiczna współrzędna Y gabinetu szefa(na planszy).
     * @param initialBudget Startowy budżet symulacji.
     */
    public Boss(String name, int x, int y, double initialBudget) {
        super(x, y);
        this.setName(name);
        this.controlRange = GameConfiguration.BOSS_CONTROL_RANGE;
        this.coffeeTimer = 0;
        this.previousBudget = initialBudget;
    }

    public void triggerMadAnimation() {
        this.madTurnsRemaining = 2;
        System.out.println("[ANIMACJA] Szef " + this.getName() + " czerwienieje ze złości w gabinecie! (Szał na 2 tury)");
    }

    /**
     * Główna metoda cyklu życia Szefa
     * Jest wywoływana w każdej turze symulacji.
     * Realizuje logikę postępowania: obsługę szału po wystąpieniu błędu,
     * sprawdzanie przerw na kawę, intensyfikację patrolu w przypadku spadku budżetu
     * oraz inicjowanie interakcji z pracownikami na sąsiednich kafelkach.
     *
     * @param board Obiekt planszy (GameBoard) umożliwiający weryfikację pozycji i typów kafelków.
     * @param sim Obiekt silnika symulacji (Simulation) dający dostęp do budżetu i listy agentów.
     */
    @Override
    public void act(GameBoard board, Simulation sim) {
        // === POPRAWIONY BLOK SZALU ===
        if (this.madTurnsRemaining > 0) {
            this.madTurnsRemaining--;
            System.out.println(" 😡 [SZAŁ] Szef " + this.getName() + " w amoku szuka winnych Fatal Errora! Pozostało tur: " + this.madTurnsRemaining);

            // W stanie szału szef ZAWSZE obiera za cel Juniorów (bo to oni psują)
            Cell juniorTarget = findWorker(sim, Junior.class, board);
            if (juniorTarget != null) {
                System.out.println("  -> [Amok] Szef namierzył Juniora i biegnie w jego stronę!");
                moveToTarget(juniorTarget, board);
            } else {
                moveRandomly(board, true);
            }

            // Po ruchu sprawdza, czy kogoś dopadł i może zwolnić
            interactWithEmployees(board, sim);
            this.previousBudget = sim.getBudget();
            return; // Wychodzimy, bo pomijamy standardową logikę (kawę, relaks itp.)
        }

        this.coffeeTimer++;
        Cell currentCell = board.getCell(getX(), getY());

        // FIX: Eliminacja transu kawowego Szefa
        if (currentCell != null && currentCell.getType().equalsIgnoreCase("coffee")) {
            if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
                this.coffeeTimer = 0;
                sim.recordCoffeeDrunk();
                System.out.println("Szef " + this.getName() + " wypił kawę na miejscu. Timer zresetowany!");
                moveRandomly(board, true);
            } else {
                // Jeśli wszedł na kawę z przypadku podczas patrolu LUB wypił i nie zdążył zejść, ucieka stąd!
                moveRandomly(board, true);
            }
            return;
        }

        if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
            Cell coffeeTarget = chooseTarget(board, sim);
            if (coffeeTarget != null) {
                moveToTarget(coffeeTarget, board);
            } else {
                moveRandomly(board, true);
            }
        }
        else {
            double chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_NORMAL;

            if (sim.getBudget() < previousBudget) {
                chanceToMove = GameConfiguration.BOSS_CHANCE_TO_MOVE_PANIC;
                System.out.println("Szef " + this.getName() + " zauważył spadek budżetu! Intensywny patrol (" + String.format("%.0f", chanceToMove * 100) + "% szans).");
            }

            if (Math.random() < chanceToMove) {
                Cell targetWorkerCell = chooseTarget(board, sim);

                if (targetWorkerCell != null) {
                    moveToTarget(targetWorkerCell, board);
                } else {
                    moveRandomly(board, true);
                }
            } else {
                System.out.println("Szef " + this.getName() + " relaksuje się w gabinecie...");
            }
        }

        interactWithEmployees(board, sim);
        this.previousBudget = sim.getBudget();
    }

    /**
     * Przemieszcza Szefa o jeden krok w kierunku wyznaczonego kafelka docelowego.
     * Wykorzystuje prosty algorytm przybliżania współrzędnych. Jeśli optymalna ścieżka
     * jest zablokowana przez przeszkodę, metoda próbuje wykonać ruch alternatywny wzdłuż jednej z osi
     * lub ostatecznie losowy krok awaryjny.
     *
     * @param target Docelowa komórka (Cell) - w tą stronę zmierza Szef (to może być np. kawa/pracownik).
     * @param board Obiekt planszy (GameBoard) odpowiedzialny za wykonanie i walidację ruchu.
     */
    private void moveToTarget(Cell target, GameBoard board) {
        if (getX() == target.getX() && getY() == target.getY()) return;

        int nextX = getX() + Integer.compare(target.getX(), getX());
        int nextY = getY() + Integer.compare(target.getY(), getY());

        if (board.moveAgent(getX(), getY(), nextX, nextY)) {
            setX(nextX);
            setY(nextY);
        } else {
            if (nextX != getX() && board.moveAgent(getX(), getY(), nextX, getY())) {
                setX(nextX);
            } else if (nextY != getY() && board.moveAgent(getX(), getY(), getX(), nextY)) {
                setY(nextY);
            } else {
                moveRandomly(board, true);
            }
        }
    }

    /**
     * Skanuje 8 sąsiednich kafelków wokół Szefa w poszukiwaniu pracowników (Worker).
     * Analizuje ich typy (Junior/Senior) oraz bieżące stany. Odpowiada za natychmiastowe
     * zwolnienie Juniora w przypadku przyłapania na paleniu na zewnątrz ("outside"), zwalnia za
     * złe wyniki wydajnościowe oraz inicjuje rozmowy z Seniorami.
     *
     * @param board Obiekt planszy służący do bezpiecznego sprawdzania sąsiednich współrzędnych.
     * @param sim Obiekt symulacji pozwalający na modyfikację stanów i sprawdzanie flag agentów.
     */
    private void interactWithEmployees(GameBoard board, Simulation sim) {
        int[][] directions = {
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            int checkX = getX() + dir[0];
            int checkY = getY() + dir[1];

            if (board.isInBounds(checkX, checkY)) {
                Cell neighborCell = board.getCell(checkX, checkY);
                Agent neighbor = neighborCell.getAgent();

                if (neighbor instanceof Worker) {
                    Worker worker = (Worker) neighbor;
                    String state = worker.getCurrentStateName();

                    // Jeśli to Junior, odpoczywa (RestingState) i kafelek to 'outside' -> ŁAPIEMY GO!
                    if (worker instanceof Junior && "RestingState".equalsIgnoreCase(state) && "outside".equalsIgnoreCase(neighborCell.getType())) {
                        System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ JUNIORA " + worker.getName() + " NA PALENIU NA ZEWNĄTRZ !!!");
                        worker.markFired();
                        continue; // Przechodzimy do kolejnego sąsiada
                    }

                    // Standardowa blokada: Szef ignoruje inne formy odpoczynku (np. legalną kawę)
                    if ("MovingToRestState".equalsIgnoreCase(state) ||
                            "RestingState".equalsIgnoreCase(state) ||
                            "coffee".equalsIgnoreCase(neighborCell.getType())) {
                        continue;
                    }

                    // Standardowe zwalnianie za złe wyniki
                    if (worker.shouldBeFired() || worker.hasTerribleMetrics()) {
                        if (worker.shouldBeFired()) {
                            System.out.println("!!! SZEF " + this.getName() + " PRZYŁAPAŁ PRACOWNIKA " + worker.getName() + " NA GORĄCYM UCZYNKU !!!");
                        } else {
                            System.out.println("!!! SZEF " + this.getName() + " ZWALNIA PRACOWNIKA " + worker.getName() + " ZA ZŁE WYNIKI (Wydajność: "
                                    + String.format("%.2f", worker.getEfficiency() * 100) + "%) !!!");
                            worker.markFired();
                        }
                    }
                    else {
                        if (worker instanceof game.agents.Senior && !state.equalsIgnoreCase("TalkingState")) {
                            System.out.println("Szef " + this.getName() + " ucina sobie przyjacielską pogawędkę z " + worker.getName() + ".");
                            worker.changeState(new game.states.TalkingState());
                        }
                    }
                }
            }
        }
    }

    /**
     * Podejmuje decyzję o wyborze priorytetowego kafelka docelowego dla Szefa w danej turze.
     * Priorytetyzacja: automat z kawą (gdy czas na przerwę), poszukiwanie Juniorów (gdy budżet spada)
     * lub wizyta u Seniorów (w normalnych warunkach).
     *
     * @param board Obiekt planszy używany do wyszukiwania wolnych kafelków o określonym typie.
     * @param sim Obiekt symulacji dostarczający informacji o aktualnych zasobach finansowych biura.
     * @return Komórka (Cell) będąca nowym celem podróży Szefa lub null, jeśli nie znaleziono celu.
     */
    private Cell chooseTarget(GameBoard board, Simulation sim) {
        if (this.coffeeTimer >= GameConfiguration.BOSS_COFFEE_INTERVAL) {
            return board.findFirstEmptyCell("coffee");
        }
        if (sim.getBudget() < previousBudget) {
            return findWorker(sim, Junior.class, board);
        } else {
            return findWorker(sim, Senior.class, board);
        }
    }

    /**
     * Przeszukuje listę aktywnych agentów w poszukiwaniu pierwszego pracownika określonej klasy.
     * Wykorzystuje mechanizm refleksji do dynamicznego filtrowania obiektów w grze.
     *
     * @param sim Obiekt symulacji, z którego pobierana jest aktualna lista wszystkich agentów w biurze.
     * @param workerClass Klasa poszukiwanego pracownika (np. Junior.class lub Senior.class).
     * @param board Obiekt planszy służący do pobrania konkretnej komórki na podstawie współrzędnych agenta.
     * @return Komórka (Cell), na której stoi znaleziony pracownik, lub null, jeśli nikt taki nie istnieje.
     */
    private Cell findWorker(Simulation sim, Class<? extends Worker> workerClass, GameBoard board) {
        for (Agent agent : sim.getAgents()) {
            if (workerClass.isInstance(agent)) {
                return board.getCell(agent.getX(), agent.getY());
            }
        }
        return null;
    }
}