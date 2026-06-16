module SymulacjaBiura {
    requires javafx.controls; //daje np button, label, HBox itd
    requires javafx.graphics;

    exports game.core; //udostepnianie pakietu game.core innym modulom
    exports game.model;
    exports game.states;
    exports game.view;
    exports game.agents;

    opens game.core to javafx.graphics;
    opens game.view to javafx.graphics;
}