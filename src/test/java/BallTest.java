import it.unical.informatica.model.Ball;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per la classe Ball
 */
class BallTest {

    @Test
    void testBallCreation() {
        Ball ball = new Ball(Ball.Color.RED, 1);

        assertEquals(Ball.Color.RED, ball.getColor());
        assertEquals(1, ball.getId());
    }

    @Test
    void testBallEquality() {
        Ball ball1 = new Ball(Ball.Color.BLUE, 1);
        Ball ball2 = new Ball(Ball.Color.BLUE, 1);
        Ball ball3 = new Ball(Ball.Color.RED, 1);
        Ball ball4 = new Ball(Ball.Color.BLUE, 2);

        assertEquals(ball1, ball2);
        assertNotEquals(ball1, ball3);
        assertNotEquals(ball1, ball4);
    }


    @Test
    void testToString() {
        Ball ball = new Ball(Ball.Color.GREEN, 5);
        String expected = "Ball{color=GREEN, id=5}";
        assertEquals(expected, ball.toString());
    }
}