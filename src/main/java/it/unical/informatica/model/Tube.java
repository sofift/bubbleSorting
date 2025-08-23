package it.unical.informatica.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Rappresenta un tubo di capacità fissa che contiene palline.
 * Convenzioni:
 * - la "cima" del tubo è l'ultima pallina inserita (LIFO)
 * - le mosse sono sempre dalla cima di un tubo alla cima di un altro
 */
public class Tube {

    private final int id;
    private final int capacity;
    private final Stack<Ball> balls;

    public Tube(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.balls = new Stack<>();
    }

    /* ------------------- Operazioni base ------------------- */

    /** Inserisce una pallina in cima. Ritorna false se il tubo è pieno. */
    public boolean addBall(Ball ball) {
        if (isFull() || ball == null) return false;
        balls.push(ball);
        return true;
    }

    /** Rimuove e restituisce la pallina in cima (o null se vuoto). */
    public Ball removeBall() {
        return isEmpty() ? null : balls.pop();
    }

    /**
     * Rimuove la pallina passata SOLO se è effettivamente la cima.
     * Utile quando il chiamante ha già la reference alla top ball.
     */
    public boolean removeBall(Ball ball) {
        if (ball == null || isEmpty()) return false;
        if (balls.peek() != ball) return false;
        balls.pop();
        return true;
    }

    /** Restituisce la pallina in cima senza rimuoverla (o null se vuoto). */
    public Ball getTopBall() {
        return isEmpty() ? null : balls.peek();
    }

    /* ------------------- Predicati di stato ------------------- */

    public boolean isEmpty() {
        return balls.isEmpty();
    }

    public boolean isFull() {
        return balls.size() >= capacity;
    }

    /** True se tutte le palline presenti hanno lo stesso colore (0/1 pallina => true). */
    public boolean isMonochromatic() {
        int n = balls.size();
        if (n <= 1) return true;
        Ball.Color first = balls.get(0).getColor();
        for (int i = 1; i < n; i++) {
            if (balls.get(i).getColor() != first) return false;
        }
        return true;
    }

    /** Pieno e monocromatico. */
    public boolean isComplete() {
        return isFull() && isMonochromatic();
    }

    /**
     * Verifica se è possibile spostare una pallina da questo tubo al "destination".
     * Regole del gioco:
     * - non puoi muovere da un tubo vuoto
     * - non puoi muovere verso un tubo pieno
     * - puoi muovere su un tubo vuoto
     * - altrimenti, il colore della cima del destination deve essere uguale alla cima di questo
     */
    public boolean canMoveTo(Tube destination) {
        if (destination == null) return false;
        if (this.isEmpty() || destination.isFull()) return false;
        if (destination.isEmpty()) return true;

        Ball topSrc = this.getTopBall();
        Ball topDst = destination.getTopBall();
        return topSrc != null && topDst != null && topSrc.getColor() == topDst.getColor();
    }

    /* ------------------- Accessori ------------------- */

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Numero di palline attualmente nel tubo. */
    public int getCurrentSize() {
        return balls.size();
    }

    /** Copia difensiva dell'elenco palline (ordine dal fondo alla cima). */
    public List<Ball> getBalls() {
        return new ArrayList<>(balls);
    }

    /** Crea una copia profonda del tubo (stesso id/capacità, palline duplicate). */
    public Tube copy() {
        Tube copy = new Tube(this.id, this.capacity);
        // manteniamo l'ordine dal fondo alla cima
        for (Ball b : this.balls) {
            copy.addBall(new Ball(b.getColor(), b.getId()));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Tube{id=" + id + ", capacity=" + capacity + ", balls=" + balls + '}';
    }
}
