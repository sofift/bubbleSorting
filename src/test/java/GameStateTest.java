
import it.unical.informatica.model.GameLevel;
import it.unical.informatica.model.GameState;
import it.unical.informatica.model.Tube;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe GameState
 */
class GameStateTest {

    private GameState gameState;

    @BeforeEach
    void setUp() {
        gameState = new GameState(GameLevel.EASY, 1);
    }

    @Test
    void testGameInitialization() {
        // Verifica che il gioco sia inizializzato correttamente
        assertEquals(GameLevel.EASY, gameState.getLevel());
        assertEquals(1, gameState.getCurrentLevelNumber());
        assertEquals(0, gameState.getMoves());
        assertFalse(gameState.isGameWon());

        // Verifica il numero di tubi
        assertEquals(GameLevel.EASY.getNumberOfTubes(), gameState.getTubes().size());

        // Verifica che ci siano tubi vuoti
        long emptyTubes = gameState.getTubes().stream()
                .mapToLong(tube -> tube.isEmpty() ? 1 : 0)
                .sum();
        assertTrue(emptyTubes >= GameLevel.EASY.getEmptyTubes());
    }

    @Test
    void testGetTubeById() {
        Tube tube = gameState.getTubeById(0);
        assertNotNull(tube);
        assertEquals(0, tube.getId());

        Tube nonExistentTube = gameState.getTubeById(999);
        assertNull(nonExistentTube);
    }

    @Test
    void testMakeMove() {
        // Trova un tubo con palline e uno vuoto
        Tube sourceeTube = null;
        Tube emptyTube = null;

        for (Tube tube : gameState.getTubes()) {
            if (!tube.isEmpty() && sourceeTube == null) {
                sourceeTube = tube;
            } else if (tube.isEmpty() && emptyTube == null) {
                emptyTube = tube;
            }
        }

        assertNotNull(sourceeTube, "Dovrebbe esserci almeno un tubo con palline");
        assertNotNull(emptyTube, "Dovrebbe esserci almeno un tubo vuoto");

        int initialMoves = gameState.getMoves();
        int initialSourceSize = sourceeTube.getCurrentSize();

        // Esegui una mossa valida
        boolean moveResult = gameState.makeMove(sourceeTube.getId(), emptyTube.getId());

        assertTrue(moveResult);
        assertEquals(initialMoves + 1, gameState.getMoves());
        assertEquals(initialSourceSize - 1, sourceeTube.getCurrentSize());
        assertEquals(1, emptyTube.getCurrentSize());
    }

    @Test
    void testInvalidMove() {
        // Prova a muovere da un tubo a se stesso
        boolean result = gameState.makeMove(0, 0);
        assertFalse(result);
        assertEquals(0, gameState.getMoves());

        // Prova a muovere da un tubo inesistente
        result = gameState.makeMove(999, 0);
        assertFalse(result);
        assertEquals(0, gameState.getMoves());
    }

    @Test
    void testGetPossibleMoves() {
        var possibleMoves = gameState.getPossibleMoves();
        assertNotNull(possibleMoves);
        assertFalse(possibleMoves.isEmpty());

        // Ogni mossa dovrebbe essere valida
        for (GameState.Move move : possibleMoves) {
            Tube fromTube = gameState.getTubeById(move.getFromTubeId());
            Tube toTube = gameState.getTubeById(move.getToTubeId());

            assertNotNull(fromTube);
            assertNotNull(toTube);
            assertTrue(fromTube.canMoveTo(toTube));
        }
    }

    @Test
    void testGameStateCopy() {
        GameState copy = gameState.copy();

        assertEquals(gameState.getLevel(), copy.getLevel());
        assertEquals(gameState.getCurrentLevelNumber(), copy.getCurrentLevelNumber());
        assertEquals(gameState.getMoves(), copy.getMoves());
        assertEquals(gameState.isGameWon(), copy.isGameWon());
        assertEquals(gameState.getTubes().size(), copy.getTubes().size());

        // Verifica che sia una copia profonda
        int originalMoves = gameState.getMoves();

        // Trova una mossa valida e applicala alla copia
        var possibleMoves = copy.getPossibleMoves();
        if (!possibleMoves.isEmpty()) {
            var move = possibleMoves.get(0);
            copy.makeMove(move.getFromTubeId(), move.getToTubeId());

            // L'originale non dovrebbe essere cambiato
            assertEquals(originalMoves, gameState.getMoves());
            assertNotEquals(gameState.getMoves(), copy.getMoves());
        }
    }
}