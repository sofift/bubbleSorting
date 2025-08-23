package it.unical.informatica.asp.ai;

import it.unical.informatica.model.*;
import java.util.List;

public final class AspInstanceBuilder {

    private AspInstanceBuilder() {}

    public static String build(GameState gs, int horizon) {
        StringBuilder sb = new StringBuilder();

        List<Tube> tubes = gs.getTubes(); // <-- usa il tuo getter reale
        int T = tubes.size();

        // Prendo la capacità dal primo tubo (tutti uguali)
        int CAP = tubes.isEmpty() ? 4 : tubes.get(0).getCapacity();

        // --- DOMINI ---
        sb.append("tube(1..").append(T).append(").\n");
        sb.append("pos(0..").append(CAP - 1).append(").\n");
        sb.append("step(0..").append(horizon).append(").\n");
        sb.append("finalStep(").append(horizon).append(").\n");

        // succ per posizioni (0->1, 1->2, ...)
        for (int p = 0; p < CAP - 1; p++) {
            sb.append("succ(").append(p).append(",").append(p + 1).append(").\n");
        }
        // prev per passi
        for (int s = 1; s <= horizon; s++) {
            sb.append("prev(").append(s).append(",").append(s - 1).append(").\n");
        }

        // --- STATO INIZIALE ---
        // init(Tubo, Posizione, Colore)
        for (int t = 0; t < T; t++) {
            Tube tube = tubes.get(t);

            // Quante palline sono effettivamente presenti
            int size = tube.getCurrentSize();
            List<Ball> balls = tube.getBalls(); // ordine: fondo..cima

            for (int p = 0; p < size; p++) {
                Ball b = balls.get(p); // p = 0 (fondo) ... size-1 (cima)
                if (b != null) {
                    int colorId = b.getId(); // o b.getId()/getColorId(), adatta qui
                    sb.append("init(")
                            .append(t + 1).append(",")
                            .append(p).append(",")
                            .append(colorId)
                            .append(").\n");
                }
            }
            // se p >= size non scrivo nulla (posizione vuota)
        }

        return sb.toString();
    }
}
