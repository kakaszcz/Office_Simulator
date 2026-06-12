package game.view;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

public class StatisticsDashboard {

    private final GridPane layout;

    private final XYChart.Series<Number, Number> budgetSeries;
    private final XYChart.Series<Number, Number> failsSeries;
    private final XYChart.Series<Number, Number> effSeries;
    private final XYChart.Series<Number, Number> coffeeSeries;

    // Interwał odświeżania wykresów (dodajemy punkt co 5 tur, aby zapobiec lagom)
    private static final int CHART_SAMPLING_INTERVAL = 5;

    public StatisticsDashboard() {
        layout = new GridPane();
        layout.setHgap(15);
        layout.setVgap(15);
        layout.setPadding(new Insets(15));

        budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budżet ($)");

        failsSeries = new XYChart.Series<>();
        failsSeries.setName("Ilość Błędów");

        effSeries = new XYChart.Series<>();
        effSeries.setName("Średnia Wydajność (0.0 - 1.0)");

        coffeeSeries = new XYChart.Series<>();
        coffeeSeries.setName("Wypite Kawy");

        layout.add(createChart("Stan Konta Firmy", budgetSeries), 0, 0);
        layout.add(createChart("Błędy w Projekcie", failsSeries), 1, 0);
        layout.add(createChart("Wydajność Zespołu", effSeries), 0, 1);
        layout.add(createChart("Spożycie Kawy", coffeeSeries), 1, 1);
    }

    private LineChart<Number, Number> createChart(String title, XYChart.Series<Number, Number> series) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.getData().add(series);
        chart.setCreateSymbols(false);
        chart.setAnimated(false); // Wyłączenie animacji drastycznie przyspiesza renderowanie

        chart.setPrefSize(350, 250);

        return chart;
    }

    public void updateCharts(int turn, double budget, int fails, double avgEfficiency, int coffees) {
        // Dodajemy punkty tylko co X tur
        if (turn % CHART_SAMPLING_INTERVAL == 0) {
            budgetSeries.getData().add(new XYChart.Data<>(turn, budget));
            failsSeries.getData().add(new XYChart.Data<>(turn, fails));
            effSeries.getData().add(new XYChart.Data<>(turn, avgEfficiency));
            coffeeSeries.getData().add(new XYChart.Data<>(turn, coffees));

            // Opcjonalne zabezpieczenie: jeśli symulacja trwa bardzo długo (np. > 300 punktów na wykresie),
            // usuwamy najstarsze punkty, żeby nie powodować wycieku pamięci
            if (budgetSeries.getData().size() > 300) {
                budgetSeries.getData().remove(0);
                failsSeries.getData().remove(0);
                effSeries.getData().remove(0);
                coffeeSeries.getData().remove(0);
            }
        }
    }

    public GridPane getLayout() {
        return layout;
    }
}