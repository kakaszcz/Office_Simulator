package test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestResources {

    public static void main(String[] args) {
        try {
            System.out.println("\n=================================================");
            System.out.println("URUCHAMIANIE SKANERA GRAFIK...");
            System.out.println("=================================================");

            // 1. Ścieżka do pliku źródłowego z logiką rysowania
            String sciezkaDoKodu = "src/main/java/game/view/GameView.java";
            File plikKodu = new File(sciezkaDoKodu);

            if (!plikKodu.exists()) {
                System.out.println(" Nie znaleziono pliku GameView.java pod ścieżką: " + plikKodu.getAbsolutePath());
                return;
            }
            String zawartoscKodu = Files.readString(Paths.get(sciezkaDoKodu));

            // 2. Ładowanie folderu zasobów
            URL imagesUrl = TestResources.class.getResource("/images");
            if (imagesUrl == null) {
                System.out.println(" Nie znaleziono folderu /images w resources!");
                return;
            }

            File folderObrazkow = new File(imagesUrl.toURI());
            File[] pliki = folderObrazkow.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (pliki == null || pliki.length == 0) {
                System.out.println("⚠️ Folder /images jest pusty lub nie zawiera plików .png");
                return;
            }

            List<String> uzywanePliki = new ArrayList<>();
            List<String> nieuzywanePliki = new ArrayList<>();

            // 3. Sprawdzanie spójności nazw plików
            for (File plik : pliki) {
                String nazwaPliku = plik.getName();
                if (nazwaPliku.startsWith(".")) continue;

                if (zawartoscKodu.contains(nazwaPliku)) {
                    uzywanePliki.add(nazwaPliku);
                } else {
                    nieuzywanePliki.add(nazwaPliku);
                }
            }

            // 4. Drukowanie raportu w konsoli
            System.out.println(" OBRAZKI POPRAWNIE PODPIĘTE W KODZIE (" + uzywanePliki.size() + "):");
            for (String f : uzywanePliki) {
                System.out.println("  [OK]  " + f);
            }

            if (!nieuzywanePliki.isEmpty()) {
                System.out.println("\n FIZYCZNE PLIKI NIEUŻYWANE W KODZIE (" + nieuzywanePliki.size() + "):");
                for (String f : nieuzywanePliki) {
                    System.out.println("  [NIEUŻYWANY] " + f);
                }
            }
            System.out.println("=================================================\n");

        } catch (Exception e) {
            System.out.println(" Wystąpił błąd podczas skanowania: " + e.getMessage());
            e.printStackTrace();
        }
    }
}