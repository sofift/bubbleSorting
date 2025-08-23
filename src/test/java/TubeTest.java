
import it.unical.informatica.model.Ball;
import it.unical.informatica.model.Tube;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe Tube
 */
class TubeTest {

    private Tube tube;
    private Ball redBall1, redBall2, blueBall1;

    @BeforeEach
    void setUp() {
        tube = new Tube(0, 4); // Capacità 4
        redBall1 = new Ball(Ball.Color.RED, 1);
        redBall2 = new Ball(Ball.Color.RED, 2);
        blueBall1 = new Ball(Ball.Color.BLUE, 3);
    }

    @Test
    void testEmptyTube() {
        assertTrue(tube.isEmpty());
        assertFalse(tube.isFull());
        assertNull(tube.getTopBall());
        assertEquals(0, tube.getCurrentSize());
        assertTrue(tube.isMonochromatic()); // Un tubo vuoto è considerato monocromatico
    }

    @Test
    void testAddBall() {
        assertTrue(tube.addBall(redBall1));
        assertFalse(tube.isEmpty());
        assertEquals(1, tube.getCurrentSize());
        assertEquals(redBall1, tube.getTopBall());
    }

    @Test
    void testRemoveBall() {
        tube.addBall(redBall1);
        tube.addBall(blueBall1);

        Ball removedBall = tube.removeBall();
        assertEquals(blueBall1, removedBall);
        assertEquals(1, tube.getCurrentSize());
        assertEquals(redBall1, tube.getTopBall());
    }

    @Test
    void testFullTube() {
        // Riempi il tubo alla capacità massima
        for (int i = 0; i < 4; i++) {
            tube.addBall(new Ball(Ball.Color.RED, i));
        }

        assertTrue(tube.isFull());
        assertFalse(tube.addBall(new Ball(Ball.Color.BLUE, 10))); // Non dovrebbe aggiungere
    }

    @Test
    void testMonochromaticTube() {
        tube.addBall(redBall1);
        tube.addBall(redBall2);

        assertTrue(tube.isMonochromatic());

        tube.addBall(blueBall1);
        assertFalse(tube.isMonochromatic());
    }

    @Test
    void testCanMoveTo() {
        Tube sourceTube = new Tube(1, 4);
        Tube destTube = new Tube(2, 4);

        // Tubo vuoto non può spostare nulla
        assertFalse(sourceTube.canMoveTo(destTube));

        // Aggiungi una pallina al tubo sorgente
        sourceTube.addBall(redBall1);

        // Può spostare in un tubo vuoto
        assertTrue(sourceTube.canMoveTo(destTube));

        // Aggiungi una pallina dello stesso colore al tubo destinazione
        destTube.addBall(redBall2);
        assertTrue(sourceTube.canMoveTo(destTube));

        // Aggiungi una pallina di colore diverso
        destTube.addBall(blueBall1);
        assertFalse(sourceTube.canMoveTo(destTube));
    }

    @Test
    void testTubeCopy() {
        tube.addBall(redBall1);
        tube.addBall(blueBall1);

        Tube copy = tube.copy();

        assertEquals(tube.getId(), copy.getId());
        assertEquals(tube.getCurrentSize(), copy.getCurrentSize());
        assertEquals(tube.getCapacity(), copy.getCapacity());

        // Verifica che sia una copia profonda
        tube.removeBall();
        assertNotEquals(tube.getCurrentSize(), copy.getCurrentSize());
    }
}