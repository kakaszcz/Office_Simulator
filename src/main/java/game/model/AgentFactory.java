package game.model;

import game.agents.Boss;
import game.agents.Junior;
import game.agents.Senior;
import game.core.GameConfiguration;

import java.util.Random;

/**
 * Fabryka agentów realizująca wzorzec projektowy Factory.
 * Odpowiada za losowanie unikalnych tożsamości (imion wraz z ikonami emoji),
 * generowanie początkowych statystyk (wydajności, doświadczenia) na podstawie
 * globalnych widełek konfiguracyjnych oraz poprawne inicjalizowanie stanów startowych agentów.
 */
public class AgentFactory {

    /** Pula imion wykorzystywana do losowania tożsamości nowo rekrutowanych pracowników. */
    private final String[] names = {
            "Mateusz", "Karolina", "Wiktoria", "Jan", "Anna", "Piotr", "Maria", "Krzysztof",
            "Kasia", "Tomasz", "Magda", "Michał", "Gienek", "Sebastian", "Brajan", "Artur",
            "Zofia", "Marek", "Barbara", "Adam", "Ewa", "Paweł", "Małgorzata", "Robert",
            "Janusz", "Grażyna", "Halina", "Mirosław", "Sławomir", "Zbigniew", "Ryszard", "Andrzej",
            "Dżesika", "Alan", "Oskar", "Kamil", "Patryk", "Nikola", "Vanessa", "Hubert",
            "Maciej", "Grzegorz", "Łukasz", "Marcin", "Rafał", "Przemysław", "Bartosz", "Wojciech",
            "Kuba", "Monika", "Agnieszka", "Natalia", "Sylwia", "Justyna", "Urszula", "Waldemar"
    };
    private final Random rand;

    /**
     * Inicjalizuje nową instancję fabryki agentów wraz z generatorem liczb pseudolosowych.
     */
    public AgentFactory() {
        this.rand = new Random();
    }

    /**
     * Tworzy i konfiguruje postać Szefa (Boss) w symulacji.
     * Dodaje dedykowaną ikonę teczki do imienia.
     *
     * @param x Początkowa współrzędna X gabinetu szefa na mapie.
     * @param y Początkowa współrzędna Y gabinetu szefa na mapie.
     * @param initialBudget Początkowy stan budżetu przekazywany szefowi do monitorowania.
     * @return Skonfigurowany obiekt klasy Boss.
     */
    public Boss createBoss(int x, int y, double initialBudget) {
        String name = "\uD83D\uDCBC " + getRandomName();
        return new Boss(name, x, y, initialBudget);
    }

    /**
     * Generuje nowego pracownika o statusie Junior.
     * Losuje jego doświadczenie z dedykowanego dla juniorów zakresu konfiguracji,
     * przypisuje ikonę dziecka/nowicjusza i ustawia go w domyślnym stanie oczekiwania na zadanie.
     *
     * @param x Współrzędna X przypisanego biurka wolnocłowego.
     * @param y Współrzędna Y przypisanego biurka wolnocłowego.
     * @return Skonfigurowany i gotowy do pracy obiekt klasy Junior.
     */
    public Junior createJunior(int x, int y) {
        double eff = getRandomEfficiency();

        double minExp = GameConfiguration.JUNIOR_START_EXP_MIN;
        double maxExp = GameConfiguration.JUNIOR_START_EXP_MAX;
        double exp = minExp + (maxExp - minExp) * rand.nextDouble();

        Junior junior = new Junior(x, y, eff, exp);
        junior.setName("\uD83D\uDC76 " + getRandomName());
        junior.changeState(new game.states.WaitingForTaskState());
        return junior;
    }

    /**
     * Generuje nowego pracownika o statusie Senior.
     * Losuje jego doświadczenie z wyższego zakresu konfiguracyjnego dla zaawansowanych programistów,
     * przypisuje ikonę mężczyzny/specjalisty i ustawia go w stanie oczekiwania na zadanie.
     *
     * @param x Współrzędna X przypisanego biurka na mapie.
     * @param y Współrzędna Y przypisanego biurka na mapie.
     * @return Skonfigurowany obiekt klasy Senior.
     */
    public Senior createSenior(int x, int y) {
        double eff = getRandomEfficiency();

        double minExp = GameConfiguration.SENIOR_START_EXP_MIN;
        double maxExp = GameConfiguration.SENIOR_START_EXP_MAX;
        double exp = minExp + (maxExp - minExp) * rand.nextDouble();

        Senior senior = new Senior(x, y, eff, exp);
        senior.setName("\uD83D\uDC68 " + getRandomName());
        senior.changeState(new game.states.WaitingForTaskState());
        return senior;
    }

    /**
     * Losuje pojedyncze imię z wbudowanej tablicy imion.
     *
     * @return Wylosowany ciąg znaków String reprezentujący imię.
     */
    private String getRandomName() {
        return names[rand.nextInt(names.length)];
    }

    /**
     * Losuje bazowy współczynnik wydajności startowej pracownika w oparciu
     * o minimalną wartość oraz dozwolony rozstęp (range size) z pliku konfiguracyjnego.
     *
     * @return Wartość typu double reprezentująca początkową efektywność.
     */
    private double getRandomEfficiency() {
        return GameConfiguration.WORKER_START_EFF_MIN + GameConfiguration.WORKER_START_EFF_RANGE_SIZE * rand.nextDouble();
    }
}