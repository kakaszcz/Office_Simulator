package game.view;

import game.core.Simulation;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SimulationStatsPanel extends VBox {

    private final Label timeLabel = new Label();
    private final Label budgetLabel = new Label();
    private final Label efficiencyLabel = new Label();
    private final Label tasksLabel = new Label();
    private final Label successRateLabel = new Label();
    private final Label coffeeLabel = new Label();
    private final Label cigarettesLabel = new Label();
    private final Label tearsLabel = new Label();
    private final Label fatalErrorLabel = new Label();

    public SimulationStatsPanel() {
        this.setSpacing(12);
        this.setPadding(new Insets(15));
        this.setMinWidth(260);

        // Ciemny, elegancki styl pasujący do nowoczesnych dashboardów managerskich
        this.setStyle("-fx-background-color: #2D3748; -fx-border-color: #4A5568; -fx-border-width: 0 0 0 2;");

        // Nagłówek sekcji
        Label title = new Label("📊 STATYSTYKI SYMULACJI");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-padding: 0 0 10 0;");

        // Wspólny styl dla tekstu z danymi
        String dataStyle = "-fx-font-size: 13px; -fx-text-fill: #E2E8F0; -fx-font-family: 'Segoe UI', Arial;";

        timeLabel.setStyle(dataStyle + "-fx-font-weight: bold; -fx-text-fill: #63B3ED;");
        budgetLabel.setStyle(dataStyle + "-fx-font-weight: bold; -fx-text-fill: #48BB78;");
        fatalErrorLabel.setStyle(dataStyle + "-fx-text-fill: #FC8181;");
        efficiencyLabel.setStyle(dataStyle);
        tasksLabel.setStyle(dataStyle);
        successRateLabel.setStyle(dataStyle);
        coffeeLabel.setStyle(dataStyle);
        cigarettesLabel.setStyle(dataStyle);
        tearsLabel.setStyle(dataStyle);

        // Separator wizualny
        Label separator = new Label("---------------------------");
        separator.setStyle("-fx-text-fill: #4A5568;");

        // Wrzucamy wszystkie komponenty do pionowego układu (VBox)
        this.getChildren().addAll(
                title,
                timeLabel,
                budgetLabel,
                fatalErrorLabel,
                efficiencyLabel,
                separator,
                tasksLabel,
                successRateLabel,
                coffeeLabel,
                cigarettesLabel,
                tearsLabel
        );
    }

    public void update(Simulation sim) {
        timeLabel.setText("📅 Czas: " + sim.getSimulationTimeFormatted() + " (Tura: " + sim.getStepCount() + ")");
        budgetLabel.setText(String.format("💰 Budżet firmy: %.2f USD", sim.getBudget()));
        fatalErrorLabel.setText("💀 Fatal Errors: " + sim.getTotalFatalErrors() + " razy");
        efficiencyLabel.setText(String.format("⚡ Średnia wydajność: %.1f%%", sim.getAverageOfficeEfficiency()));
        tasksLabel.setText("✅ Sukcesy: " + sim.getTotalTasksSuccess() + "  |  ❌ Porażki: " + sim.getTotalTasksFailed());
        successRateLabel.setText(String.format("📈 Skuteczność zadań: %.1f%%", sim.getSuccessRate()));
        coffeeLabel.setText("☕ Wypite kawy: " + sim.getTotalCoffeesDrank() + " szt.");
        cigarettesLabel.setText("🚬 Wypalone papierosy: " + sim.getTotalCigarettesSmoked() + " szt.");
        tearsLabel.setText("😭 Wylane łzy Juniorów: " + sim.getTotalTearsShed() + " razy");
    }
}