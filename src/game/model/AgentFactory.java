package game.model;

import java.util.Random;

public class AgentFactory {
    private final String[] names = {"Mateusz", "Karolina", "Wiktoria", "Jan", "Anna", "Piotr", "Maria", "Krzysztof", "Kasia", "Tomasz", "Magda", "Michał", "Gienek", "Sebastian", "Brajan", "Artur",  "Zofia", "Marek", "Barbara", "Adam", "Ewa", "Paweł", "Małgorzata", "Robert"};
    private final Random rand;

    public AgentFactory() {
        this.rand = new Random();
    }
    
    public Boss createBoss(int x, int y, double initialBudget) {
        String name = getRandomName();
        return new Boss(name, x, y, initialBudget);
    }

    public Junior createJunior(int x, int y) {
        double eff = getRandomEfficiency();
        double exp = 0.1 + (0.4 - 0.1) * rand.nextDouble();

        Junior junior = new Junior(x, y, eff, exp);
        junior.setName(getRandomName());
        junior.changeState(new game.states.WaitingForTaskState());
        return junior;
    }

    public Senior createSenior(int x, int y) {
        double eff = getRandomEfficiency();
        double exp = 0.6 + (0.95 - 0.6) * rand.nextDouble();

        Senior senior = new Senior(x, y, eff, exp);
        senior.setName(getRandomName());
        senior.changeState(new game.states.WaitingForTaskState());
        return senior;
    }

    private String getRandomName() {
        return names[rand.nextInt(names.length)];
    }

    private double getRandomEfficiency() {
        return 0.4 + 0.5 * rand.nextDouble();
    }
}