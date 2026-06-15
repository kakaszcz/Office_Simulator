# Symulacja Biura IT

## Opis projektu

**Symulacja Biura IT** to aplikacja napisana w Javie, przedstawia działanie fikcyjnego biura programistycznego. W symulacji występują pracownicy o różnych rolach, szef, zadania, budżet firmy, błędy produkcyjne oraz statystyki kadrowe.

Celem projektu jest pokazanie działania prostego systemu agentowego, w którym pracownicy poruszają się po planszy, zmieniają stany, wykonują zadania, odpoczywają, popełniają błędy i wpływają na kondycję firmy.

Projekt ma formę aplikacji okienkowej z interfejsem graficznym wykonanym w JavaFX.

---

## Główne funkcje

* Konfiguracja początkowej liczby Juniorów i Seniorów.
* Ustawienie początkowego budżetu firmy.
* Symulacja pracy biura IT w turach.
* Automatyczne przydzielanie zadań pracownikom.
* Obliczanie sukcesów i porażek zadań.
* System błędów Juniorów oraz awarii krytycznych typu Fatal Error.
* Spadek budżetu przez pensje i kary za błędy.
* Zakończenie symulacji w przypadku bankructwa.
* Panel HR pokazujący statystyki aktywnych i zwolnionych pracowników.
* Zwalnianie pracowników za złe wyniki lub przyłapanie przez szefa.
* Automatyczne zatrudnianie nowego Juniora po zwolnieniu poprzedniego.
* Obsługa kawy, odpoczynku, papierosów, płaczu i rozmów z szefem.
* Przycisk wyjścia z symulacji i powrotu do ekranu startowego.
* Grafiki pracowników, biurek i elementów planszy w stylu pixel-art.

---

## Role w symulacji

### Junior

Junior to mniej doświadczony pracownik. Może wykonywać zadania, ale ma większą szansę na popełnienie błędu. Jego szansa na błąd zależy od doświadczenia oraz aktualnej efektywności.

Junior może otrzymać motywacyjny boost od szefa, jeśli szef znajdzie się w jego pobliżu. Boost zwiększa efektywność Juniora i jest zapisywany w statystykach kadrowych.

Junior posiada licznik aktualnych błędów oraz licznik błędów całkowitych z całej kariery. Dzięki temu historia błędów nie znika po resecie bieżących pomyłek.

### Senior

Senior to bardziej doświadczony pracownik. Ma bonus do wydajności i może osiągać lepsze wyniki niż Junior.

Senior może rozmawiać z szefem. Liczba rozmów z szefem jest zapisywana w statystykach pracownika w panelu HR.

### Szef

Szef porusza się po biurze i reaguje na sytuację firmy. Może patrolować biuro, kierować się do kawy, reagować na spadek budżetu oraz wpadać w szał po awarii krytycznej.

Szef:

* rozmawia z Seniorami,
* daje boosty Juniorom,
* może zwalniać pracowników za złe wyniki,
* może przyłapać Juniora na odpoczynku poza biurem,
* reaguje na Fatal Error.

---

## Mechanika budżetu

Firma posiada budżet początkowy ustawiany na ekranie startowym. Budżet zmienia się w trakcie symulacji.

Budżet może spadać przez:

* wypłaty dla pracowników,
* kary za awarie krytyczne,
* negatywne skutki błędów.

Jeśli budżet spadnie do zera lub poniżej, symulacja kończy się bankructwem.

---

## Panel HR

Panel HR pokazuje informacje o pracownikach oraz ich statystyki. Dla każdego pracownika zapisywane są między innymi:

* imię,
* rola,
* status aktywny lub zwolniony,
* efektywność,
* doświadczenie,
* liczba przeżytych tur,
* ukończone zadania,
* nieudane zadania,
* wypite kawy,
* wypalone papierosy,
* liczba płaczów,
* rozmowy z szefem dla Seniorów,
* boosty od szefa dla Juniorów,
* naprawione błędy.

Panel rozróżnia statystyki Juniorów i Seniorów, aby pokazywać odpowiedni typ interakcji z szefem.

---

## Technologie

Projekt wykorzystuje:

* Java 21
* JavaFX
* Maven
* JUnit 5
* Maven Surefire Plugin

---

## Struktura projektu

```text
src/main/java/game/agents
```

Klasy agentów, między innymi pracownicy, Juniorzy, Seniorzy i Szef.

```text
src/main/java/game/core
```

Główna logika symulacji, konfiguracja gry, kontroler gry, aplikacja JavaFX i obsługa HR.

```text
src/main/java/game/model
```

Model planszy, komórki planszy, rekordy pracowników, fabryka agentów i logika pomocnicza.

```text
src/main/java/game/states
```

Stany pracowników, np. oczekiwanie na zadanie, praca, odpoczynek, rozmowa, powrót do biurka.

```text
src/main/java/game/view
```

Widok gry, panel HR i elementy interfejsu użytkownika.

```text
src/main/resources/images
```

Grafiki używane w aplikacji.

```text
src/test/java
```

Testy jednostkowe projektu.

---

## Uruchamianie projektu (Quick Start)

Projekt najlepiej uruchamiać w IntelliJ IDEA. 
Wymagania: środowisko **Java JDK 21** oraz narzędzie **Maven**
Główną klasą uruchomieniową jest `MainApp.java`.
Uruchomienie przez Terminal (alternatywa):
   ```bash
   mvn clean package
   mvn javafx:run
Po uruchomieniu aplikacji pojawia się ekran startowy, na którym można ustawić liczbę Juniorów, liczbę Seniorów oraz początkowy budżet.

---

## Testy

Projekt korzysta z JUnit 5.

Testy znajdują się w folderze:

```text
src/test/java
```

Testy można uruchomić bezpośrednio z poziomu IntelliJ IDEA.

Można je również uruchomić z panelu Maven:

```text
Maven → Lifecycle → test
```

Projekt posiada solidne pokrycie testami jednostkowymi (JUnit 5) weryfikującymi reguły biznesowe. Obejmują one m.in.:
* **Silnik Symulacji (`SimulationTest`):** Zabezpieczenia budżetu, upływ czasu, matematyka kar i statystyk.
* **Kadry (`HRManagerTest`):** Weryfikacja cyklu życia pracownika oraz testy odporności archiwum (Defensive Copies).
* **Agenci (`JuniorTest`, `BossTest`):** Zależności zmęczenia od szansy na błąd, liczniki sąsiedztwa szefa, stany szału i resetowanie metryk.
* **Diagnostyka Zasobów (`TestResources`):** Autorski skrypt linter'a (Asset Scanner) weryfikujący poprawność mapowania plików graficznych `.png` w kodzie źródłowym gry.

Testy można uruchomić z poziomu środowiska IDE lub za pomocą komendy: `mvn test` (Maven -> Lifecycle -> test).

---
## Przykładowy przebieg (Sample Run)

Oto scenariusz tego, jak zachowuje się aplikacja po prawidłowym uruchomieniu i jak wygląda przykładowa rozgrywka:

1. **Konfiguracja początkowa:** Po włączeniu programu pojawia się ekran startowy. Użytkownik widzi domyślne parametry (5 Juniorów, 3 Seniorów oraz budżet 2000.0) i może je dostosować przed kliknięciem przycisku startu.
2. **Inicjalizacja biura:** Po zatwierdzeniu konfiguracji ładuje się graficzna mapa biura w stylu pixel-art (wymiary planszy to 16x10 kafelków). Na swoich pozycjach startowych pojawiają się pracownicy oraz Szef.
3. **Cykl życia biura i logi:** W trakcie trwania tur pracownicy wykonują zadania. W konsoli środowiska IntelliJ na bieżąco drukują się logi systemowe, np.:
   `[STAN] Szef siedzi przy biurku i czeka.`
4. **Zdarzenia losowe i interakcje:**
   * Co 3 tury rozdzielane są nowe taski.
   * Co 30 tur z budżetu pobierane są stałe pensje dla pracowników.
   * Jeśli Junior popełni błąd, na planszy widać animację płaczu, a w konsoli pojawia się informacja o spadku efektywności.
   * Gdy podejdzie do niego Szef, w konsoli i statystykach odnotowywany jest "motywacyjny boost".
5. **Koniec symulacji (Bankructwo):** Gra toczy się autonomicznie do momentu, gdy przez nagromadzenie błędów i wypłaty pensji budżet firmy spadnie do wartości `<= 0`. W tym momencie aplikacja blokuje dalszy ruch, zatrzymuje pętlę gry i wyświetla ekran końcowy z komunikatem o bankructwie oraz przyciskiem powrotu do menu.

---

## Status projektu

Projekt jest w trakcie rozwoju.

Zrealizowane zostały między innymi:

* podstawowa symulacja biura,
* role Juniora, Seniora i Szefa,
* panel HR,
* ekran startowy,
* ekran bankructwa,
* obsługa grafik,
* interakcje szefa z pracownikami,
* podstawowe testy jednostkowe.

Planowane lub możliwe dalsze prace:

* dodanie kolejnych testów jednostkowych,
* dopracowanie statystyk końcowych,
* dalsze poprawki UI,
* uporządkowanie grafik i zasobów,
* ewentualne dodanie większej liczby mechanik symulacji.

---

## Autorzy

Karolina Kaszczyszyn, Wiktoria Pala, Mateusz Ojewski

---


