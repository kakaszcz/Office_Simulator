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

    private final VBox scrollContent;
    private final FlowPane juniorCardsContainer;
    private final FlowPane seniorCardsContainer;

    // REFAKTOR: Licznik optymalizacji odświeżania panelu HR
    private int updateTickCounter = 0;
    private static final int HR_UPDATE_INTERVAL = 10; // Aktualizacja kart pracowników co 10 tur

    public HRDashboard() {
        layout = new VBox(10);
        layout.setPadding(new Insets(15));

        topStatsLabel = new Label("STATYSTYKI PRACOWNIKÓW");
        topStatsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        juniorCardsContainer = new FlowPane();
        juniorCardsContainer.setHgap(15);
        juniorCardsContainer.setVgap(15);

        seniorCardsContainer = new FlowPane();
        seniorCardsContainer.setHgap(15);
        seniorCardsContainer.setVgap(15);

        Label juniorHeader = new Label("👶 JUNIORZY");
        juniorHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4a90e2; -fx-padding: 5 0 5 0;");

        Label seniorHeader = new Label("👨‍💻 SENIORZY"); // Poprawiony czytelny emoji seniora
        seniorHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-padding: 15 0 5 0;");

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
        // REFAKTOR: Pozwalamy na aktualizację tylko raz na 10 kroków symulacji.
        // Zapobiega to gigantycznemu obciążeniu Garbage Collectora i usuwa mikroprzycięcia gry.
        updateTickCounter++;
        if (updateTickCounter % HR_UPDATE_INTERVAL != 0) {
            return;
        }

        // Czyszczenie starych kafelków z obu kontenerów
        juniorCardsContainer.getChildren().clear();
        seniorCardsContainer.getChildren().clear();

        // 1. Rozdzielamy AKTYWNYCH pracowników
        for (EmployeeRecord record : hr.getActiveRecords()) {
            VBox card = createCard(record);
            if ("Junior".equalsIgnoreCase(record.role)) {
                juniorCardsContainer.getChildren().add(card);
            } else if ("Senior".equalsIgnoreCase(record.role)) {
                seniorCardsContainer.getChildren().add(card);
            }
        }

        // 2. Dorzucamy ZWOLNIONYCH na koniec listy w swojej grupie
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
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

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

        Label tearsLbl = new Label("Łzy: 😭 " + record.timesCried);
        tearsLbl.setStyle("-fx-text-fill: #555555;");

        String bossInteractionText;

        if ("Junior".equalsIgnoreCase(record.role)) {
            bossInteractionText = "Boosty od szefa: ⚡ " + record.bossBoosts;
        } else if ("Senior".equalsIgnoreCase(record.role)) {
            bossInteractionText = "Rozmowy z szefem: 👔 " + record.bossTalks;
        } else {
            bossInteractionText = "Interakcje z szefem: 0";
        }

        Label bossInteractionLbl = new Label(bossInteractionText);
        bossInteractionLbl.setStyle("-fx-text-fill: #555555;");
        bossInteractionLbl.setWrapText(true);

        card.getChildren().addAll(
                nameLbl,
                stateLbl,
                expLbl,
                effLbl,
                tasksLbl,
                turnsLbl,
                addictionsLbl,
                tearsLbl,
                bossInteractionLbl
        );

        if (record.bugsRepaired > 0) {
            Label fixesLbl = new Label("Fixy: 🛠️ " + record.bugsRepaired);
            fixesLbl.setStyle("-fx-text-fill: #555555;");
            card.getChildren().add(fixesLbl);
        }

        return card;
    }

    public VBox getLayout() {
        return layout;
    }
}