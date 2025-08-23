
import it.unical.informatica.model.Ball;
import it.unical.informatica.model.GameLevel;
import it.unical.informatica.model.GameState;
import it.unical.informatica.model.Tube;
import org.junit.jupiter.api.Test;


class DemoTest {

    @Test
    void demoGameplay() {
        System.out.println("=== DEMO: Bubble Sorting Game ===\n");

        // Crea un nuovo gioco con livello facile
        GameState game = new GameState(GameLevel.EASY, 1);

        System.out.println("Configurazione del livello:");
        System.out.println("- Livello: " + game.getLevel().getDisplayName());
        System.out.println("- Tubi totali: " + game.getLevel().getNumberOfTubes());
        System.out.println("- Colori: " + game.getLevel().getNumberOfColors());
        System.out.println("- Tubi vuoti: " + game.getLevel().getEmptyTubes());
        System.out.println();

        // Mostra lo stato iniziale
        printGameState(game, "Stato iniziale");

        // Esegui alcune mosse
        var possibleMoves = game.getPossibleMoves();
        System.out.println("Mosse possibili: " + possibleMoves.size());

        int moveCount = 0;
        for (var move : possibleMoves) {
            if (moveCount >= 3) break; // Limita a 3 mosse per la demo

            System.out.println("\n--- Mossa " + (moveCount + 1) + " ---");
            System.out.println("Tentativo di spostare da tubo " + move.getFromTubeId() +
                    " a tubo " + move.getToTubeId());

            boolean success = game.makeMove(move.getFromTubeId(), move.getToTubeId());
            System.out.println("Risultato: " + (success ? "SUCCESSO" : "FALLIMENTO"));

            if (success) {
                printGameState(game, "Dopo la mossa " + (moveCount + 1));
                moveCount++;
            }
        }

        System.out.println("\n=== Fine Demo ===");
        System.out.println("Mosse totali eseguite: " + game.getMoves());
        System.out.println("Gioco vinto: " + (game.isGameWon() ? "SÌ" : "NO"));
    }

    private void printGameState(GameState game, String title) {
        System.out.println("--- " + title + " ---");

        for (Tube tube : game.getTubes()) {
            System.out.print("Tubo " + tube.getId() + ": ");

            if (tube.isEmpty()) {
                System.out.println("[VUOTO]");
            } else {
                System.out.print("[");
                var balls = tube.getBalls();
                for (int i = 0; i < balls.size(); i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(balls.get(i).getColor().name().charAt(0)); // Prima lettera del colore
                }
                System.out.print("] ");

                if (tube.isMonochromatic()) {
                    System.out.print("(MONO)");
                }
                if (tube.isComplete()) {
                    System.out.print("(COMPLETO)");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    @Test
    void demoAllLevels() {
        System.out.println("=== DEMO: Tutti i Livelli ===\n");

        for (GameLevel level : GameLevel.values()) {
            System.out.println("Livello: " + level.getDisplayName());
            System.out.println("- Tubi: " + level.getNumberOfTubes());
            System.out.println("- Colori: " + level.getNumberOfColors());
            System.out.println("- Palline per colore: " + level.getBallsPerColor());
            System.out.println("- Palline totali: " + level.getTotalBalls());
            System.out.println("- Tubi vuoti: " + level.getEmptyTubes());

            // Mostra i colori disponibili
            System.out.print("- Colori disponibili: ");
            Ball.Color[] colors = level.getAvailableColors();
            for (int i = 0; i < colors.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(colors[i].name());
            }
            System.out.println("\n");
        }
    }
}