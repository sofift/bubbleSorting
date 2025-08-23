package it.unical.informatica.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generatore livelli per Bubble Sorting, coerente con:
 * - Tube(int id, int capacity)
 * - Tube.addBall(Ball)
 * - Ball(Color color, int id)   <-- vedi copy() nella tua Tube
 */
public final class LevelGenerator {

    /** Capacità fissa dei tubi (Ball Sort classico) */
    private static final int CAP = 4;

    LevelGenerator() { }

    /**
     * Genera un GameState casuale in base alla difficoltà:
     * FACILE:  K=4 colori,   T=6 tubi (4 pieni + 2 vuoti)
     * MEDIO:   K=5 colori,   T=7 tubi (5 pieni + 2 vuoti)
     * DIFFICILE: K=7 colori, T=9 tubi (7 pieni + 2 vuoti)
     */
    public static GameState generate(Difficolta diff) {
        int colors;
        int tubes;

        switch (diff) {
            case FACILE    -> { colors = 4; tubes = 6; }
            case MEDIO     -> { colors = 5; tubes = 7; }
            case DIFFICILE -> { colors = 7; tubes = 9; }
            default -> throw new IllegalArgumentException("Difficoltà non valida: " + diff);
        }

        // 1) pool di colori: per ogni colore c, CAP palline
        List<Integer> pool = new ArrayList<>(colors * CAP);
        for (int c = 0; c < colors; c++) {
            for (int i = 0; i < CAP; i++) pool.add(c);
        }
        Collections.shuffle(pool, ThreadLocalRandom.current());

        // 2) crea i tubi (id 1..tubes, capacità CAP)
        List<Tube> ts = new ArrayList<>(tubes);
        for (int t = 0; t < tubes; t++) {
            ts.add(new Tube(t + 1, CAP));
        }

        // 3) riempi i primi 'colors' tubi con 4 palline ciascuno; gli ultimi 2 restano vuoti
        int idx = 0;
        int nextBallId = 1; // id progressivo per coerenza con Ball(color, id)
        for (int t = 0; t < colors; t++) {
            Tube tube = ts.get(t);
            for (int p = 0; p < CAP; p++) {
                int colorIndex = pool.get(idx++);
                Ball.Color colorEnum = Ball.Color.values()[colorIndex];
                tube.addBall(new Ball(colorEnum, nextBallId++));
            }
        }

        // 4) restituisci lo stato di gioco
        // Se esiste il factory 'fromTubes', usa questa riga:
        return GameState.fromTubes(ts);

        // Se NON hai GameState.fromTubes(List<Tube>), puoi creare un aiutino statico nel GameState
        // o, in emergenza, aggiungere in GameState un costruttore che accetti i tubi.
        // Esempio temporaneo (da usare solo se il factory manca):
        // GameState gs = new GameState(/* difficoltà/level come serve a te */);
        // gs.setTubes(ts); return gs;
    }

    public static int getCapacity() { return CAP; }
}
