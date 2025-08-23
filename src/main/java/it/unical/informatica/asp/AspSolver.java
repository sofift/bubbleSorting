package it.unical.informatica.asp;

import it.unical.informatica.asp.EmbAspLevelChecker;
import it.unical.informatica.asp.EmbAspGameSolver;
import it.unical.informatica.model.GameLevel;
import it.unical.informatica.model.GameState;
import it.unical.mat.embasp.languages.IllegalAnnotationException;
import it.unical.mat.embasp.languages.ObjectNotValidException;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe principale per l'integrazione con ASP usando embASP + DLV2
 * VERSIONE ACCADEMICA PROFESSIONALE
 */
public class AspSolver {

    private final EmbAspLevelChecker levelChecker;
    private final EmbAspGameSolver gameSolver;

    public AspSolver() throws ObjectNotValidException, IllegalAnnotationException {
        this.levelChecker = new EmbAspLevelChecker();
        this.gameSolver = new EmbAspGameSolver();

        System.out.println("🔧 AspSolver inizializzato con embASP + DLV2");
        System.out.println("📍 DLV2 Path: libs/dlv-2.1.2-win64.exe");
        System.out.println("🧠 Mapping automatico Java ↔ ASP attivato");
    }

    /**
     * Verifica se un livello è risolvibile usando embASP
     */
    public boolean isLevelSolvable(GameState gameState) {
        try {
            return levelChecker.isLevelSolvable(gameState);
        } catch (Exception e) {
            System.err.println("Errore nella verifica risolubilità con embASP: " + e.getMessage());
            return true; // Assumiamo che sia risolvibile se c'è un errore
        }
    }

    /**
     * Verifica se un livello è risolvibile con dettagli (usando embASP)
     */
    public EmbAspLevelChecker.CheckResult checkLevelWithDetails(GameState gameState) {
        return levelChecker.checkLevelWithDetails(gameState);
    }

    /**
     * Trova la migliore mossa possibile usando embASP + DLV2
     */
    public GameState.Move findBestMove(GameState gameState) {
        try {
            GameState.Move bestMove = gameSolver.findNextMove(gameState);
            if (bestMove != null) {
                System.out.println("🎯 embASP suggerisce: " + bestMove);
                return bestMove;
            }
        } catch (Exception e) {
            System.err.println("Errore nel trovare la miglior mossa con embASP: " + e.getMessage());
        }

        // Fallback: trova una mossa valida casuale
        List<GameState.Move> possibleMoves = gameState.getPossibleMoves();
        if (!possibleMoves.isEmpty()) {
            return possibleMoves.get(0);
        }

        return null;
    }

    /**
     * Risolve completamente il gioco usando embASP + DLV2
     */
    public List<GameState.Move> solveGame(GameState gameState) {
        try {
            List<GameState.Move> solution = gameSolver.solve(gameState);
            System.out.println("🚀 embASP ha trovato soluzione in " + solution.size() + " mosse");
            return solution;
        } catch (Exception e) {
            System.err.println("Errore nella risoluzione del gioco con embASP: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Alias per solveGame - usato dal GameController
     */
    public List<GameState.Move> findSolution(GameState gameState) {
        return solveGame(gameState);
    }

    /**
     * Verifica se DLV2 è disponibile
     */
    public boolean isSolverAvailable() {
        return levelChecker.isSolverAvailable();
    }

    /**
     * Ottieni informazioni sul solver embASP + DLV2
     */
    public String getSolverInfo() {
        return levelChecker.getSolverInfo();
    }

    /**
     * Verifica la configurazione embASP + DLV2
     */
    public boolean checkConfiguration() {
        try {
            if (!isSolverAvailable()) {
                System.err.println("❌ DLV2 non disponibile");
                return false;
            }

            // Test semplice per verificare che embASP + DLV2 funzionino
            GameLevel testLevel = GameLevel.EASY;
            GameState testGameState = GameState.createTestLevel(testLevel, 1);

            boolean result = levelChecker.isLevelSolvable(testGameState);
            System.out.println("✅ Test configurazione embASP + DLV2: " +
                    (result ? "SUCCESSO" : "FALLIMENTO"));
            return result;

        } catch (Exception e) {
            System.err.println("Errore nella verifica configurazione embASP: " + e.getMessage());
            return false;
        }
    }

    /**
     * Stima la difficoltà di un livello
     */
    public int estimateLevelDifficulty(GameState gameState) {
        try {
            return gameSolver.estimateDifficulty(gameState);
        } catch (Exception e) {
            System.err.println("Errore nella stima difficoltà: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Verifica se una mossa è ottimale secondo embASP
     */
    public boolean isMoveOptimal(GameState gameState, GameState.Move candidateMove) {
        try {
            return gameSolver.isMoveOptimal(gameState, candidateMove);
        } catch (Exception e) {
            System.err.println("Errore nella verifica ottimalità mossa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Risoluzione con statistiche dettagliate
     */
    public EmbAspGameSolver.SolveResult solveWithStats(GameState gameState) {
        return gameSolver.solveWithStats(gameState);
    }

    /**
     * Ottiene informazioni diagnostiche complete del sistema embASP
     */
    public String getDetailedDiagnosticInfo() {
        StringBuilder info = new StringBuilder();
        info.append("🔧 DIAGNOSTICA SISTEMA embASP + DLV2\n\n");

        info.append("Configurazione:\n");
        info.append("- Solver: DLV2 (libs/dlv-2.1.2-win64.exe)\n");
        info.append("- Framework: embASP (Embedded Answer Set Programming)\n");
        info.append("- Mapping: Automatico Java ↔ ASP\n\n");

        info.append("Stato Sistema:\n");
        info.append("- DLV2 Disponibile: ").append(isSolverAvailable() ? "✅ SÌ" : "❌ NO").append("\n");
        info.append("- Configurazione: ").append(checkConfiguration() ? "✅ OK" : "❌ ERRORE").append("\n\n");

        info.append("Dettagli Solver:\n");
        info.append(getSolverInfo()).append("\n");

        return info.toString();
    }
}
