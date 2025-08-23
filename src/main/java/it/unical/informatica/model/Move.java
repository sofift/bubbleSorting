// src/main/java/it/unical/informatica/model/Move.java
package it.unical.informatica.model;

import java.util.Objects;

/**
 * Mossa elementare: sposta la pallina in cima dal tubo "from" al tubo "to".
 * Gli indici dei tubi sono 1-based (coerenti con l'ASP e con la UI).
 */
public class Move {

    private final int fromTubeId;
    private final int toTubeId;

    public Move(int fromTubeId, int toTubeId) {
        this.fromTubeId = fromTubeId;
        this.toTubeId = toTubeId;
    }

    public int getFromTubeId() {
        return fromTubeId;
    }

    public int getToTubeId() {
        return toTubeId;
    }

    @Override
    public String toString() {
        return "Move{" + fromTubeId + "->" + toTubeId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return fromTubeId == move.fromTubeId && toTubeId == move.toTubeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromTubeId, toTubeId);
    }
}
