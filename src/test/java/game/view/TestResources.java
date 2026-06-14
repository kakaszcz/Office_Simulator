package game.view;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Autonomiczne narzędzie diagnostyczne (Asset Integrity Scanner).
 * Klasa nie jest klasycznym testem JUnit, lecz skryptem wykonywalnym, który analizuje
 * spójność zasobów graficznych (.png) znajdujących się w folderze resources/images
 * z ich faktycznym wykorzystaniem (tekstowym odwołaniem) w klasie renderującej GameView.java.
 * Pomaga w identyfikacji martwych assetów oraz zapobiega błędom braku tekstur.
 */
public class TestResources {

    /**
     * Główny punkt wejścia skryptu skanującego.
     * Odczytuje kod źródłowy widoku gry, parsuje zawartość katalogu z grafikami,
     * dokonuje mapowania krzyżowego i generuje czytelny raport w konsoli.
     *
     * @param args Standardowe argumenty linii poleceń (nieużywane).
     */
    public static void main(String[] args) {
        try {
            System.out.println("\n=================================================");
            System.out.println("URUCHAMIANIE SKANERA GRAFIK...");
            System.out.println("=================================================");

            // 1. Ścieżka do pliku źródłowego z logiką rysowania
            String sciezkaDoKodu = "src/main/java/game/view/GameView.java";
            File plikKodu = new File(sciezkaDoKodu);

            // Weryfikacja fizycznego istnienia pliku źródłowego widoku przed odczytem stringa
            if (!plikKodu.exists()) {
                System.out.println(" Nie znaleziono pliku GameView.java pod ścieżką: " + plikKodu.getAbsolutePath());
                return;
            }
            // Wczytanie całego kodu źródłowego pliku .java do pamięci jako jeden ciąg tekstowy
            String zawartoscKodu = Files.readString(Paths.get(sciezkaDoKodu));

            // 2. Ładowanie folderu zasobów skompilowanych (ClassPath Resources)
            URL imagesUrl = TestResources.class.getResource("/images");
            if (imagesUrl == null) {
                System.out.println(" Nie znaleziono folderu /images w resources!");
                return;
            }

            // Konwersja URL z zasobów na obiekt pliku sieciowego (URI), aby móc operować na strukturze katalogu
            File folderObrazkow = new File(imagesUrl.toURI());
            // Filtrowanie zawartości katalogu przy użyciu lambdy - akceptujemy wyłącznie rozszerzenia .png
            File[] pliki = folderObrazkow.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (pliki == null || pliki.length == 0) {
                System.out.println("⚠️ Folder /images jest pusty lub nie zawiera plików .png");
                return;
            }

            // Listy zbierające wyniki klasyfikacji assetów
            List<String> uzywanePliki = new ArrayList<>();
            List<String> nieuzywanePliki = new ArrayList<>();

            // 3. Sprawdzanie spójności nazw plików (Mapowanie Krzyżowe)
            for (File plik : pliki) {
                String nazwaPliku = plik.getName();
                // Ignorowanie ukrytych plików systemowych (np. .DS_Store na macOS lub miniatury Windows)
                if (nazwaPliku.startsWith(".")) continue;

                // Sprawdzenie, czy nazwa pliku (np. "worker.png") występuje jako literał tekstowy w GameView.java
                if (zawartoscKodu.contains(nazwaPliku)) {
                    uzywanePliki.add(nazwaPliku);
                } else {
                    nieuzywanePliki.add(nazwaPliku);
                }
            }

            // 4. Drukowanie raportu w konsoli systemowej
            System.out.println(" OBRAZKI POPRAWNIE PODPIĘTE W KODZIE (" + uzywanePliki.size() + "):");
            for (String f : uzywanePliki) {
                System.out.println("  [OK]  " + f);
            }

            // Wyświetlenie sekcji ostrzeżeń tylko w przypadku znalezienia zbędnych plików
            if (!nieuzywanePliki.isEmpty()) {
                System.out.println("\n FIZYCZNE PLIKI NIEUŻYWANE W KODZIE (" + nieuzywanePliki.size() + "):");
                for (String f : nieuzywanePliki) {
                    System.out.println("  [NIEUŻYWANY] " + f);
                }
            }
            System.out.println("=================================================\n");

        } catch (Exception e) {
            // Bezpieczne przechwycenie błędów operacji na plikach (IOException) lub błędów parsowania URI
            System.out.println(" Wystąpił błąd podczas skanowania: " + e.getMessage());
            e.printStackTrace();
        }
    }
}