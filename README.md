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

## Uruchamianie projektu

Projekt najlepiej uruchamiać w IntelliJ IDEA.

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

Aktualnie dodane testy obejmują logikę klasy `Junior`, między innymi:

* wzrost szansy błędu przy spadku efektywności,
* reset bieżących błędów bez resetowania historii błędów,
* zapamiętywanie informacji o sąsiedztwie szefa,
* licznik boostów otrzymanych od szefa.

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


