package game.view;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

public class StatisticsDashboard {

    private final GridPane layout;

    // Serie danych (linie na wykresach)
    private final XYChart.Series<Number, Number> budgetSeries;
    private final XYChart.Series<Number, Number> failsSeries;
    private final XYChart.Series<Number, Number> effSeries;
    private final XYChart.Series<Number, Number> coffeeSeries;

    public StatisticsDashboard() {
        layout = new GridPane();
        layout.setHgap(10);
        layout.setVgap(10);
        layout.setPadding(new Insets(10));
        layout.setPrefWidth(500);

        budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budżet ($)");

        failsSeries = new XYChart.Series<>();
        failsSeries.setName("Ilość Błędów");

        effSeries = new XYChart.Series<>();
        effSeries.setName("Średnia Wydajność (0.0 - 1.0)");

        coffeeSeries = new XYChart.Series<>();
        coffeeSeries.setName("Wypite Kawy");

        // Układamy 4 wykresy w siatce 2x2
        layout.add(createChart("Stan Konta Firmy", budgetSeries), 0, 0);
        layout.add(createChart("Błędy w Projekcie", failsSeries), 1, 0);
        layout.add(createChart("Wydajność Zespołu", effSeries), 0, 1);
        layout.add(createChart("Spożycie Kawy", coffeeSeries), 1, 1);
    }

    private LineChart<Number, Number> createChart(String title, XYChart.Series<Number, Number> series) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false); // Oś X przesuwa się razem z czasem

        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.getData().add(series);
        chart.setCreateSymbols(false); // Wyłącza kropki na wykresie (zostawia samą ładną linię)
        chart.setPrefSize(250, 200); // Małe rozmiary, żeby zmieściły się cztery
        chart.setAnimated(false); // Wyłącza domyślne animacje, żeby wykres nadążał za grą

        return chart;
    }

    // Ta metoda będzie wywoływana co turę, żeby narysować nowy punkt!
    public void updateCharts(int turn, double budget, int fails, double avgEfficiency, int coffees) {
        budgetSeries.getData().add(new XYChart.Data<>(turn, budget));
        failsSeries.getData().add(new XYChart.Data<>(turn, fails));
        effSeries.getData().add(new XYChart.Data<>(turn, avgEfficiency));
        coffeeSeries.getData().add(new XYChart.Data<>(turn, coffees));
    }

    public GridPane getLayout() {
        return layout;
    }
}