package game.view;

import game.core.HRManager;
import game.model.EmployeeRecord;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class HRDashboard {

    private final VBox layout;
    private final Label topStatsLabel;

    // Kontener główny wewnątrz ScrollPane, który trzyma nagłówki i siatki kart
    private final VBox scrollContent;

    // Osobne kontenery na kafelki dla Juniorów i Seniorów
    private final FlowPane juniorCardsContainer;
    private final FlowPane seniorCardsContainer;

    public HRDashboard() {
        layout = new VBox(10);
        layout.setPadding(new Insets(15));

        topStatsLabel = new Label("STATYSTYKI PRACOWNIKÓW");
        topStatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Inicjalizacja kontenerów na karty
        juniorCardsContainer = new FlowPane();
        juniorCardsContainer.setHgap(15);
        juniorCardsContainer.setVgap(15);

        seniorCardsContainer = new FlowPane();
        seniorCardsContainer.setHgap(15);
        seniorCardsContainer.setVgap(15);

        // Przygotowujemy nagłówki dla sekcji
        Label juniorHeader = new Label("👶 JUNIORZY");
        juniorHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a90e2; -fx-padding: 5 0 5 0;");

        Label seniorHeader = new Label("\uD83D\uDC68 SENIORZY");
        seniorHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-padding: 15 0 5 0;");

        // Główny pionowy kontener, który ułoży sekcje jedna pod drugą
        scrollContent = new VBox(10);
        scrollContent.setPadding(new Insets(5));
        scrollContent.getChildren().addAll(
                juniorHeader, juniorCardsContainer,
                new Separator(),
                seniorHeader, seniorCardsContainer
        );

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        layout.getChildren().addAll(topStatsLabel, scroll);
    }

    public void update(HRManager hr) {
        // Czyszczenie starych kafelków z obu kontenerów
        juniorCardsContainer.getChildren().clear();
        seniorCardsContainer.getChildren().clear();

        // 1. Najpierw rozdzielamy AKTYWNYCH pracowników
        for (EmployeeRecord record : hr.getActiveRecords()) {
            VBox card = createCard(record);
            if ("Junior".equalsIgnoreCase(record.role)) {
                juniorCardsContainer.getChildren().add(card);
            } else if ("Senior".equalsIgnoreCase(record.role)) {
                seniorCardsContainer.getChildren().add(card);
            }
        }

        // 2. Potem do tych samych kontenerów dorzucamy ZWOLNIONYCH (będą na końcu listy w swojej grupie)
        for (EmployeeRecord record : hr.getFiredRecords()) {
            VBox card = createCard(record);
            if ("Junior".equalsIgnoreCase(record.role)) {
                juniorCardsContainer.getChildren().add(card);
            } else if ("Senior".equalsIgnoreCase(record.role)) {
                seniorCardsContainer.getChildren().add(card);
            }
        }
    }

    private VBox createCard(EmployeeRecord record) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setPrefWidth(220);

        // Kolor tła i obramowania kafelka
        if (record.isActive) {
            card.setStyle("-fx-border-color: #2ce62c; -fx-border-radius: 5; -fx-border-width: 2; -fx-background-color: #f0fff0;");
        } else {
            card.setStyle("-fx-border-color: #ff3333; -fx-border-radius: 5; -fx-border-width: 2; -fx-background-color: #fff0f0;");
        }

        Label nameLbl = new Label(record.name + " (" + record.role + ")");
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;"); // <--- dodane -fx-text-fill

        Label stateLbl = new Label(record.isActive ? "🟢 W biurze" : "🔴 Zwolniony");
        stateLbl.setStyle("-fx-text-fill: #555555;");

        Label expLbl = new Label(String.format("Doświadczenie: %.2f", record.experience));
        expLbl.setStyle("-fx-text-fill: #555555;");

        Label effLbl = new Label(String.format("Wydajność: %.2f", record.efficiency));
        effLbl.setStyle("-fx-text-fill: #555555;");

        Label tasksLbl = new Label("Zadania: " + record.tasksCompleted + " ✓ / " + record.tasksFailed + " ✗");
        tasksLbl.setStyle("-fx-text-fill: #555555;");

        Label turnsLbl = new Label("Czas w firmie (tury): " + record.turnsAlive);
        turnsLbl.setStyle("-fx-text-fill: #555555;");

        Label addictionsLbl = new Label("Kawa: ☕ " + record.coffeesDrunk + " | Fajki: 🚬 " + record.cigarettesSmoked);
        addictionsLbl.setStyle("-fx-text-fill: #555555;");

        Label extraLbl = new Label("Łzy: 😭 " + record.timesCried + " | Rozmowy z szefem: 👔 " + record.bossTalks);
        extraLbl.setStyle("-fx-text-fill: #555555;");

        if (record.bugsRepaired > 0) {
            extraLbl.setText(extraLbl.getText() + " | Fixy: 🛠️ " + record.bugsRepaired);
        }

        card.getChildren().addAll(nameLbl, stateLbl, expLbl, effLbl, tasksLbl, turnsLbl, addictionsLbl, extraLbl);
        return card;
    }

    public VBox getLayout() {
        return layout;
    }
}