// src/main/java/it/unical/informatica/ai/SolveLevelWorkflowService.java
package it.unical.informatica.asp.ai;

import it.unical.informatica.model.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.time.Duration;
import java.util.List;

public class SolveLevelWorkflowService extends Service<SolveLevelWorkflowService.Result> {

    public static class Result {
        public final GameState level;
        public final List<Move> plan;
        public Result(GameState level, List<Move> plan) {
            this.level = level;
            this.plan = plan;
        }
    }

    private final Difficolta diff;
    private final AspRunner runner;
    private final int horizon;
    private final Duration timeout;
    private final int maxTries;

    public SolveLevelWorkflowService(Difficolta diff, AspRunner runner,
                                     int horizon, Duration timeout, int maxTries) {
        this.diff = diff;
        this.runner = runner;
        this.horizon = horizon;
        this.timeout = timeout;
        this.maxTries = maxTries;
    }

    @Override
    protected Task<Result> createTask() {
        return new Task<>() {
            @Override
            protected Result call() throws Exception {
                updateTitle("AI Planner");
                updateProgress(0, 3);
                updateMessage("Genero livello casuale...");

                GameState candidate = null;
                boolean ok = false;
                String facts = null;

                for (int attempt = 1; attempt <= maxTries; attempt++) {
                    if (isCancelled()) throw new InterruptedException("Cancellato");

                    candidate = LevelGenerator.generate(diff);

                    // Costruisci istanza ASP
                    facts = AspInstanceBuilder.build(candidate, horizon);

                    updateMessage("Check risolvibilità (tentativo " + attempt + "/" + maxTries + ")...");
                    String outCheck = runner.runCheck(facts, timeout);

                    if (AspParsers.parseCheckReached(outCheck)) {
                        ok = true;
                        break;
                    }
                }

                if (!ok) {
                    throw new IllegalStateException("Nessun livello risolvibile trovato entro i tentativi massimi.");
                }

                updateProgress(1, 3);
                updateMessage("Risolvo con ASP...");
                String outSolve = runner.runSolve(facts, timeout);

                List<Move> plan = AspParsers.parsePlan(outSolve);
                if (plan.isEmpty()) {
                    throw new IllegalStateException("Nessun piano trovato (move/3 assente).");
                }

                updateProgress(3, 3);
                updateMessage("Fatto.");
                return new Result(candidate, plan);
            }
        };
    }
}
