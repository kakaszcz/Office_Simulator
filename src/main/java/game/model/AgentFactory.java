package game.model;

import game.agents.Boss;
import game.agents.Junior;
import game.agents.Senior;
import game.core.GameConfiguration;

import java.util.Random;

public class AgentFactory {
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

    public AgentFactory() {
        this.rand = new Random();
    }

    public Boss createBoss(int x, int y, double initialBudget) {
        String name = "\uD83D\uDCBC " + getRandomName();
        return new Boss(name, x, y, initialBudget);
    }

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

    private String getRandomName() {
        return names[rand.nextInt(names.length)];
    }

    private double getRandomEfficiency() {
        return GameConfiguration.WORKER_START_EFF_MIN + GameConfiguration.WORKER_START_EFF_RANGE_SIZE * rand.nextDouble();
    }
}