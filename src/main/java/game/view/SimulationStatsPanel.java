package game.view;

import game.core.Simulation;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Klasa SimulationStatsPanel odpowiada za prezentację globalnych metryk działania biura.
 * Dziedziczy po klasie {@link VBox}, tworząc boczny panel informacyjny w ciemnej,
 * nowoczesnej stylistyce (Dark Mode).
 * Agreguje i formatuje kluczowe wskaźniki efektywności (KPI), takie jak czas, budżet,
 * stabilność systemu (Fatal Errors) oraz statystyki behawioralne i emocjonalne agentów.
 */
public class SimulationStatsPanel extends VBox {

    /** Etykieta prezentująca sformatowany czas systemowy oraz numer bieżącej tury. */
    private final Label timeLabel = new Label();
    /** Etykieta wyświetlająca aktualny stan konta finansowego firmy. */
    private final Label budgetLabel = new Label();
    /** Etykieta monitorująca średnią wydajność całego zespołu w biurze. */
    private final Label efficiencyLabel = new Label();
    /** Etykieta prezentująca sumaryczną liczbę zadań zakończonych sukcesem i porażką. */
    private final Label tasksLabel = new Label();
    /** Etykieta obliczająca procentowy stosunek zadań udanych do wszystkich podjętych. */
    private final Label successRateLabel = new Label();
    /** Etykieta zliczająca całkowitą konsumpcję kawy przez pracowników. */
    private final Label coffeeLabel = new Label();
    /** Etykieta rejestrująca łączną liczbę przerw na papierosa na zewnątrz biura. */
    private final Label cigarettesLabel = new Label();
    /** Etykieta zliczająca incydenty załamania emocjonalnego (płaczu) u Juniorów. */
    private final Label tearsLabel = new Label();
    /** Etykieta ostrzegawcza prezentująca łączną liczbę krytycznych awarii systemu. */
    private final Label fatalErrorLabel = new Label();

    /**
     * Konstruuje panel statystyk, definiuje jego geometrię, marginesy wewnętrzne
     * oraz nakłada arkusze stylów inline CSS (kolorystyka, typografia Seoge UI / Arial).
     * Inicjalizuje i układa komponenty w pionowym stosie układu.
     */
    public SimulationStatsPanel() {
        // Konfiguracja marginesów i wymiarów kontenera pionowego
        this.setSpacing(12);
        this.setPadding(new Insets(15));
        this.setMinWidth(260);

        // Ciemny, elegancki styl pasujący do nowoczesnych dashboardów managerskich
        this.setStyle("-fx-background-color: #2D3748; -fx-border-color: #4A5568; -fx-border-width: 0 0 0 2;");

        // Nagłówek sekcji panelu bocznego
        Label title = new Label("📊 STATYSTYKI SYMULACJI");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-padding: 0 0 10 0;");

        // Wspólny bazowy styl dla etykiet tekstowych prezentujących dane liczbowe
        String dataStyle = "-fx-font-size: 13px; -fx-text-fill: #E2E8F0; -fx-font-family: 'Segoe UI', Arial;";

        // Indywidualne formatowanie kolorystyczne dla kluczowych wskaźników (kolory akcentowe)
        timeLabel.setStyle(dataStyle + "-fx-font-weight: bold; -fx-text-fill: #63B3ED;"); // Jasnoniebieski dla czasu
        budgetLabel.setStyle(dataStyle + "-fx-font-weight: bold; -fx-text-fill: #48BB78;"); // Zielony dla finansów
        fatalErrorLabel.setStyle(dataStyle + "-fx-text-fill: #FC8181;"); // Pastelowa czerwień dla błędów krytycznych

        // Nałożenie standardowego stylu na pozostałe metryki biurowe
        efficiencyLabel.setStyle(dataStyle);
        tasksLabel.setStyle(dataStyle);
        successRateLabel.setStyle(dataStyle);
        coffeeLabel.setStyle(dataStyle);
        cigarettesLabel.setStyle(dataStyle);
        tearsLabel.setStyle(dataStyle);

        // Separator wizualny dzielący panel na sekcję systemowo-finansową oraz operacyjną
        Label separator = new Label("---------------------------");
        separator.setStyle("-fx-text-fill: #4A5568;");

        // Wrzucamy wszystkie komponenty do pionowego układu (VBox) zgodnie z hierarchią prezentacji
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

    /**
     * Synchronizuje zawartość tekstową etykiet z najnowszym stanem obiektów biznesowych.
     * Odpowiada za formatowanie zmiennych zmiennoprzecinkowych do czytelnych
     * reprezentacji tekstowych oraz procentowych.
     *
     * @param sim Instancja głównego silnika symulacji (Model) stanowiąca źródło danych.
     */
    public void update(Simulation sim) {
        // Aktualizacja danych czasowych i finansowych
        timeLabel.setText("📅 Czas: " + sim.getSimulationTimeFormatted() + " (Tura: " + sim.getStepCount() + ")");
        budgetLabel.setText(String.format("💰 Budżet firmy: %.2f USD", sim.getBudget()));
        fatalErrorLabel.setText("💀 Fatal Errors: " + sim.getTotalFatalErrors() + " razy");

        // Mapowanie i zaokrąglanie średniej wydajności biura do jednego miejsca po przecinku
        efficiencyLabel.setText(String.format("⚡ Średnia wydajność: %.1f%%", sim.getAverageOfficeEfficiency()));
        tasksLabel.setText("✅ Sukcesy: " + sim.getTotalTasksSuccess() + "  |  ❌ Porażki: " + sim.getTotalTasksFailed());
        successRateLabel.setText(String.format("📈 Skuteczność zadań: %.1f%%", sim.getSuccessRate()));

        // Aktualizacja liczników konsumpcyjnych oraz wskaźników psychofizycznych zespołu
        coffeeLabel.setText("☕ Wypite kawy: " + sim.getTotalCoffeesDrank() + " szt.");
        cigarettesLabel.setText("🚬 Wypalone papierosy: " + sim.getTotalCigarettesSmoked() + " szt.");
        tearsLabel.setText("😭 Wylane łzy Juniorów: " + sim.getTotalTearsShed() + " razy");
    }
}