package game.states;

import game.core.Simulation;
import game.model.GameBoard;
import game.model.Worker;

public interface WorkerState {
    // Metoda wywoływana jednorazowo przy zmianie stanu (odpowiednik akcji "entry" z diagramu)
    void enter(Worker worker);

    // Metoda wywoływana co turę z pętli głównej
    void act(Worker worker, GameBoard board, Simulation sim);
}


