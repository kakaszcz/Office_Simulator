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

/* public enum WorkerState {
    WAITING, //siedzi przy biurku czeka na taska
    MOVING_TO_REST, //idzie na kawe lub na dwor
    RESTING, //na kawie czy na dorze
    MOVING_TO_DESK,
    WORKING, //robi taska przy biurku
    CRYING, //junior co zrobil faila
    REPAIRING, //senior naprawia faila juniora
    TALKING, //gada z szefem

    }

 */

