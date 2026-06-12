package game.agents;

import game.core.Simulation;
import game.core.GameConfiguration;
import game.model.*;

public class Boss extends Agent {

    private int controlRange;
    private int coffeeTimer;
    private double previousBudget;
    private int madTurnsRemaining = 0;

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

    @Override
    public void act(GameBoard board, Simulation sim) {
        if (this.madTurnsRemaining > 0) {
            this.madTurnsRemaining--;
            System.out.println("  -> Szef " + this.getName() + " rwie włosy z głowy z powodu Fatal Errora! Pozostało tur szału: " + this.madTurnsRemaining);
            interactWithEmployees(board, sim);
            this.previousBudget = sim.getBudget();
            return;
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

                    if ("MovingToRestState".equalsIgnoreCase(state) ||
                            "RestingState".equalsIgnoreCase(state) ||
                            neighborCell.getType().equalsIgnoreCase("coffee")) {
                        continue;
                    }

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

    private Cell findWorker(Simulation sim, Class<? extends Worker> workerClass, GameBoard board) {
        for (Agent agent : sim.getAgents()) {
            if (workerClass.isInstance(agent)) {
                return board.getCell(agent.getX(), agent.getY());
            }
        }
        return null;
    }
}