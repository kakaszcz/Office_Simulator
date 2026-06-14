package game.view;

import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

/**
 * Klasa StatisticsDashboard odpowiada za wizualizację trendów i danych historycznych symulacji.
 * Uporządkowana w strukturze siatki {@link GridPane} ($2 \times 2$), wyświetla wykresy liniowe
 * dotyczące kluczowych płaszczyzn gry: finansów, stabilności projektu, wydajności oraz zasobów.
 * Klasa implementuje strategię redukcji liczby próbek (Data Sampling) oraz sztywny limit
 * pamięci podręcznej serii danych, co zapobiega wyciekom pamięci oraz spadkom wydajności renderowania.
 */
public class StatisticsDashboard {

    /** Główny kontener siatki przechowujący i pozycjonujący cztery wykresy liniowe. */
    private final GridPane layout;

    /** Seria danych historycznych reprezentująca stan konta firmy. */
    private final XYChart.Series<Number, Number> budgetSeries;
    /** Seria danych historycznych reprezentująca narastającą liczbę błędów/porażek. */
    private final XYChart.Series<Number, Number> failsSeries;
    /** Seria danych historycznych monitorująca fluktuacje średniej wydajności zespołu. */
    private final XYChart.Series<Number, Number> effSeries;
    /** Seria danych historycznych zliczająca globalne spożycie kawy w czasie. */
    private final XYChart.Series<Number, Number> coffeeSeries;

    /** Interwał próbkowania danych (Data Downsampling Rate). Punkty dopisywane są co X tur. */
    private static final int CHART_SAMPLING_INTERVAL = 5;

    /**
     * Konstruuje pulpit analityczny, inicjalizuje serie danych dla każdego wykresu,
     * konfiguruje odstępy wewnątrz siatki oraz wywołuje fabrykę komponentów
     * w celu osadzenia wykresów w odpowiednich komórkach macierzy układu.
     */
    public StatisticsDashboard() {
        layout = new GridPane();
        layout.setHgap(15);
        layout.setVgap(15);
        layout.setPadding(new Insets(15));

        // Inicjalizacja i konfiguracja etykiet serii danych
        budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budżet ($)");

        failsSeries = new XYChart.Series<>();
        failsSeries.setName("Ilość Błędów");

        effSeries = new XYChart.Series<>();
        effSeries.setName("Średnia Wydajność (0.0 - 1.0)");

        coffeeSeries = new XYChart.Series<>();
        coffeeSeries.setName("Wypite Kawy");

        // Rozmieszczenie wykresów w siatce GridPane (kolumna, wiersz)
        layout.add(createChart("Stan Konta Firmy", budgetSeries), 0, 0);
        layout.add(createChart("Błędy w Projekcie", failsSeries), 1, 0);
        layout.add(createChart("Wydajność Zespołu", effSeries), 0, 1);
        layout.add(createChart("Spożycie Kawy", coffeeSeries), 1, 1);
    }

    /**
     * Metoda fabrykująca (Factory Method) tworząca i konfigurująca instancję wykresu liniowego.
     * Wyłącza domyślne animacje oraz renderowanie symboli węzłów (kropek), co drastycznie
     * odciąża wątek graficzny JavaFX Application Thread przy częstych aktualizacjach stanu.
     *
     * @param title  Tytuł wyświetlany w nagłówku wykresu.
     * @param series Referencja do powiązanej serii danych, która ma być wizualizowana.
     * @return Sformatowany i gotowy do wyświetlenia obiekt klasy {@link LineChart}.
     */
    private LineChart<Number, Number> createChart(String title, XYChart.Series<Number, Number> series) {
        // Konfiguracja osi odciętych (X) oraz rzędnych (Y) z dynamicznym dopasowaniem zakresu
        NumberAxis xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.getData().add(series);
        chart.setCreateSymbols(false); // Wyłączenie rysowania punktów węzłowych dla płynności linii
        chart.setAnimated(false);      // Wyłączenie animacji drastycznie przyspiesza renderowanie

        chart.setPrefSize(350, 250);

        return chart;
    }

    /**
     * Aktualizuje serie danych wykresów o bieżący punkt pomiarowy symulacji.
     * Metoda filtruje wywołania na podstawie {@link #CHART_SAMPLING_INTERVAL}.
     * Zawiera bezpieczny mechanizm czyszczenia bufora danych (Sliding Window),
     * utrzymujący maksymalnie 300 najnowszych punktów na wykres w celu optymalizacji pamięci RAM.
     *
     * @param turn          Aktualny numer tury symulacji (oś X).
     * @param budget        Bieżący stan budżetu (oś Y wykresu 1).
     * @param fails         Bieżąca liczba błędów/niepowodzeń (oś Y wykresu 2).
     * @param avgEfficiency Aktualna średnia wydajność pracowników (oś Y wykresu 3).
     * @param coffees       Aktualna suma wypitych kaw (oś Y wykresu 4).
     */
    public void updateCharts(int turn, double budget, int fails, double avgEfficiency, int coffees) {
        // Dodajemy punkty tylko co X tur w celu redukcji narzutu obliczeniowego
        if (turn % CHART_SAMPLING_INTERVAL == 0) {
            budgetSeries.getData().add(new XYChart.Data<>(turn, budget));
            failsSeries.getData().add(new XYChart.Data<>(turn, fails));
            effSeries.getData().add(new XYChart.Data<>(turn, avgEfficiency));
            coffeeSeries.getData().add(new XYChart.Data<>(turn, coffees));

            // Implementacja okna przesuwnego (Sliding Window Strategy) dla optymalizacji struktur pamięciowych
            if (budgetSeries.getData().size() > 300) {
                budgetSeries.getData().remove(0);
                failsSeries.getData().remove(0);
                effSeries.getData().remove(0);
                coffeeSeries.getData().remove(0);
            }
        }
    }

    /**
     * Zwraca główny kontener siatki układu graficznego pulpitów analitycznych.
     *
     * @return Węzeł typu {@link GridPane} zawierający zbiór wykresów.
     */
    public GridPane getLayout() {
        return layout;
    }
}