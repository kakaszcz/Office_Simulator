package game;

import java.util.Random;

public class AgentFactory {
    private final String[] names = {"Mateusz", "Karolina", "Wiktoria", "Jan", "Anna", "Piotr", "Maria", "Krzysztof", "Kasia", "Tomasz", "Magda", "Michał", "Gienek", "Sebastian", "Brajan", "Artur",  "Zofia", "Marek", "Barbara", "Adam", "Ewa", "Paweł", "Małgorzata", "Robert"};
    private final Random rand;

    public AgentFactory() {
        this.rand = new Random();
    }

    public Boss createBoss(int x, int y) {
        String name = getRandomName();
        Boss boss = new Boss(name, x, y);
        boss.setName(name);
        return boss;
    }

    public Junior createJunior(int x, int y) {
        double eff = getRandomEfficiency();
        // Doświadczenie od 10% do 40%
        double exp = 0.1 + (0.4 - 0.1) * rand.nextDouble();

        Junior junior = new Junior(x, y, eff, exp);
        junior.setName(getRandomName());
        return junior;
    }

    public Senior createSenior(int x, int y) {
        double eff = getRandomEfficiency();
        // Doświadczenie od 60% do 95%
        double exp = 0.6 + (0.95 - 0.6) * rand.nextDouble();

        Senior senior = new Senior(x, y, eff, exp);
        senior.setName(getRandomName());
        return senior;
    }

    // --- Metody pomocnicze ---

    private String getRandomName() {
        return names[rand.nextInt(names.length)];
    }

    private double getRandomEfficiency() {
        // Wydajność 0.4 - 0.9 dla każdego pracownika
        return 0.4 + 0.5 * rand.nextDouble();
    }
}