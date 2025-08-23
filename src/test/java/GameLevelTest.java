
import it.unical.informatica.model.Ball;
import it.unical.informatica.model.GameLevel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per l'enum GameLevel
 */
class GameLevelTest {

    @Test
    void testEasyLevel() {
        GameLevel easy = GameLevel.EASY;

        assertEquals(1, easy.getLevelNumber());
        assertEquals("Facile", easy.getDisplayName());
        assertEquals(6, easy.getNumberOfTubes());
        assertEquals(4, easy.getNumberOfColors());
        assertEquals(4, easy.getBallsPerColor());
        assertEquals(4, easy.getTubeCapacity());
        assertEquals(2, easy.getEmptyTubes()); // 6 tubi - 4 colori = 2 tubi vuoti
        assertEquals(16, easy.getTotalBalls()); // 4 colori × 4 palline = 16 palline totali
    }

    @Test
    void testMediumLevel() {
        GameLevel medium = GameLevel.MEDIUM;

        assertEquals(2, medium.getLevelNumber());
        assertEquals("Medio", medium.getDisplayName());
        assertEquals(7, medium.getNumberOfTubes());
        assertEquals(5, medium.getNumberOfColors());
        assertEquals(4, medium.getBallsPerColor());
        assertEquals(4, medium.getTubeCapacity());
        assertEquals(2, medium.getEmptyTubes()); // 7 tubi - 5 colori = 2 tubi vuoti
        assertEquals(20, medium.getTotalBalls()); // 5 colori × 4 palline = 20 palline totali
    }

    @Test
    void testHardLevel() {
        GameLevel hard = GameLevel.HARD;

        assertEquals(3, hard.getLevelNumber());
        assertEquals("Difficile", hard.getDisplayName());
        assertEquals(9, hard.getNumberOfTubes());
        assertEquals(7, hard.getNumberOfColors());
        assertEquals(4, hard.getBallsPerColor());
        assertEquals(4, hard.getTubeCapacity());
        assertEquals(2, hard.getEmptyTubes()); // 9 tubi - 7 colori = 2 tubi vuoti
        assertEquals(28, hard.getTotalBalls()); // 7 colori × 4 palline = 28 palline totali
    }

    @Test
    void testAvailableColors() {
        // Test per il livello facile
        Ball.Color[] easyColors = GameLevel.EASY.getAvailableColors();
        assertEquals(4, easyColors.length);
        assertEquals(Ball.Color.RED, easyColors[0]);
        assertEquals(Ball.Color.BLUE, easyColors[1]);
        assertEquals(Ball.Color.GREEN, easyColors[2]);
        assertEquals(Ball.Color.YELLOW, easyColors[3]);

        // Test per il livello medio
        Ball.Color[] mediumColors = GameLevel.MEDIUM.getAvailableColors();
        assertEquals(5, mediumColors.length);
        assertEquals(Ball.Color.ORANGE, mediumColors[4]);

        // Test per il livello difficile
        Ball.Color[] hardColors = GameLevel.HARD.getAvailableColors();
        assertEquals(7, hardColors.length);
        assertEquals(Ball.Color.PURPLE, hardColors[5]);
        assertEquals(Ball.Color.PINK, hardColors[6]);
    }

    @Test
    void testToString() {
        assertEquals("Facile (6 tubi, 4 colori)", GameLevel.EASY.toString());
        assertEquals("Medio (7 tubi, 5 colori)", GameLevel.MEDIUM.toString());
        assertEquals("Difficile (9 tubi, 7 colori)", GameLevel.HARD.toString());
    }

    @Test
    void testAllLevelsHaveCorrectEmptyTubes() {
        // Ogni livello dovrebbe avere almeno 2 tubi vuoti per permettere i movimenti
        for (GameLevel level : GameLevel.values()) {
            assertTrue(level.getEmptyTubes() >= 2,
                    "Il livello " + level.getDisplayName() + " dovrebbe avere almeno 2 tubi vuoti");
        }
    }
}