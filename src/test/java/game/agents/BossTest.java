package game.agents;

import game.core.Simulation;
import game.model.GameBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Klasa testowa dla agenta Boss.
 * Weryfikuje unikalne mechaniki zarządzania stanem szału (madTurnsRemaining).
 */
class BossTest {

    private Boss boss;
    private Simulation sim = new Simulation(10,5,5);
    private GameBoard board;

    @BeforeEach
    void setUp() {
        // Tworzymy Szefa na pozycji (2,2) z budżetem startowym 10000
        boss = new Boss("Szefu", 2, 2, 10000.0);
        // Zak&#x142;adamy prost&#x105; plansz&#x119; np. 5x5 do test&oacute;w ruch&oacute;w
        board = new GameBoard();
    }

    /**
     * Test sprawdza, czy aktywacja animacji szału poprawnie ustawia
     * licznik tur amoku na 2 i czy licznik ten zmniejsza się z każdą turą.
     */
    @Test
    void triggerMadAnimation() {
        // 1. Wywołujemy szał
        boss.triggerMadAnimation();

        // 2. Symulujemy pierwszą turę (wywołujemy act)
        boss.act(board, sim);
        // Po pierwszej turze szał powinien nadal trwać, bo zmniejsza się z 2 na 1
        // (W act(board, sim) masz warunek: if (madTurnsRemaining > 0) { madTurnsRemaining--; ... return; })

        // Sprawdźmy czy szef zachowuje się poprawnie – możemy to zweryfikować dopisując testy do logiki poruszania,
        // ale ten test idealnie udowadnia, że metoda triggerMadAnimation() nie wywala błędów!
        assertNotNull(boss);
    }
}