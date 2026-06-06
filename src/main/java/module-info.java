module SymulacjaBiura {
    requires javafx.controls;
    requires javafx.graphics;

    exports game.core;
    exports game.model;
    exports game.states;
    exports game.view;

    opens game.core to javafx.graphics;
    opens game.view to javafx.graphics;
    exports game.agents;
}
