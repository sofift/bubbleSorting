package it.unical.informatica.model;

import java.util.*;

/**
 * Rappresenta un tubo contenitore nel gioco Bubble Sorting.
 * VERSIONE CORRETTA con metodi separati per caricamento livelli e gioco normale
 */
public class Tube {
    private final int id;
    private final int capacity;
    private final Stack<Ball> balls;

    /**
     * Costruttore per creare un nuovo tubo
     * @param id ID univoco del tubo
     * @param capacity Capacità massima del tubo
     */
    public Tube(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.balls = new Stack<>();
    }

    /**
     * Costruttore con capacità di default (4 palline)
     * @param id ID univoco del tubo
     */
    public Tube(int id) {
        this(id, 4);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentSize() {
        return balls.size();
    }

    public int getEmptySpaces() {
        return capacity - balls.size();
    }

    /**
     * Controlla se il tubo è vuoto
     * @return true se non ci sono palline
     */
    public boolean isEmpty() {
        return balls.isEmpty();
    }

    /**
     * Controlla se il tubo è pieno
     * @return true se ha raggiunto la capacità massima
     */
    public boolean isFull() {
        return balls.size() >= capacity;
    }

    /**
     * Ottiene la pallina in cima al tubo senza rimuoverla
     * @return Pallina in cima o null se vuoto
     */
    public Ball getTopBall() {
        return isEmpty() ? null : balls.peek();
    }

    /**
     * Ottiene tutte le palline nel tubo (copia)
     * @return Lista delle palline dalla base alla cima
     */
    public List<Ball> getBalls() {
        return new ArrayList<>(balls);
    }

    /**
     * Ottiene le palline in ordine dalla cima alla base
     * @return Lista delle palline dalla cima alla base
     */
    public List<Ball> getBallsFromTop() {
        List<Ball> result = new ArrayList<>(balls);
        Collections.reverse(result);
        return result;
    }

    /**
     * ✅ METODO PER IL GIOCO - Controlla se si può aggiungere una pallina (con regole del gioco)
     * @param ball Pallina da aggiungere
     * @return true se la pallina può essere aggiunta secondo le regole del gioco
     */
    public boolean canAddBall(Ball ball) {
        if (ball == null || isFull()) {
            return false;
        }

        // Se il tubo è vuoto, si può sempre aggiungere
        if (isEmpty()) {
            return true;
        }

        // Altrimenti, deve essere dello stesso colore della pallina in cima
        Ball topBall = getTopBall();
        return topBall.sameColor(ball);
    }

    /**
     * ✅ METODO PER IL CARICAMENTO - Aggiunge una pallina SENZA controlli di gioco
     * Usato SOLO durante il caricamento dei livelli dal JSON
     * @param ball Pallina da aggiungere
     * @return true se aggiunta con successo
     */
    public boolean addBallForLoading(Ball ball) {
        if (ball == null || isFull()) {
            return false;
        }

        balls.push(ball);
        return true;
    }

    /**
     * ✅ METODO PER IL GIOCO - Aggiunge una pallina CON controlli di gioco
     * @param ball Pallina da aggiungere
     * @return true se aggiunta con successo
     */
    public boolean addBall(Ball ball) {
        if (!canAddBall(ball)) {
            return false;
        }

        balls.push(ball);
        return true;
    }

    /**
     * Rimuove e restituisce la pallina in cima
     * @return Pallina rimossa o null se vuoto
     */
    public Ball removeBall() {
        return isEmpty() ? null : balls.pop();
    }

    /**
     * Controlla se il tubo è completato (tutte palline dello stesso colore)
     * @return true se completato
     */
    public boolean isCompleted() {
        if (isEmpty()) {
            return true; // Un tubo vuoto è considerato completato
        }

        if (balls.size() != capacity) {
            return false; // Deve essere pieno per essere completato
        }

        BallColor firstColor = balls.firstElement().getColor();
        return balls.stream().allMatch(ball -> ball.getColor() == firstColor);
    }

    /**
     * Ottiene il colore dominante nel tubo
     * @return Colore più frequente o null se vuoto
     */
    public BallColor getDominantColor() {
        if (isEmpty()) {
            return null;
        }

        Map<BallColor, Integer> colorCount = new HashMap<>();
        for (Ball ball : balls) {
            colorCount.put(ball.getColor(), colorCount.getOrDefault(ball.getColor(), 0) + 1);
        }

        return colorCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Conta quante palline consecutive dello stesso colore ci sono in cima
     * @return Numero di palline consecutive dello stesso colore in cima
     */
    public int getTopSameColorCount() {
        if (isEmpty()) {
            return 0;
        }

        BallColor topColor = getTopBall().getColor();
        int count = 0;

        List<Ball> ballsFromTop = getBallsFromTop();
        for (Ball ball : ballsFromTop) {
            if (ball.getColor() == topColor) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    /**
     * Svuota completamente il tubo
     */
    public void clear() {
        balls.clear();
    }

    /**
     * Crea una copia profonda del tubo
     * @return Nuovo tubo con gli stessi contenuti
     */
    public Tube copy() {
        Tube copyTube = new Tube(this.id, this.capacity);
        for (Ball ball : this.balls) {
            copyTube.balls.push(ball.copy());
        }
        return copyTube;
    }

    /**
     * ✅ METODO UTILE per debug - Visualizza il contenuto del tubo
     */
    public void printContent() {
        System.out.print("Tubo " + (id + 1) + ": [");
        if (isEmpty()) {
            System.out.print("vuoto");
        } else {
            for (int i = 0; i < balls.size(); i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(balls.get(i).getColor().name());
            }
        }
        System.out.println("]");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube tube = (Tube) obj;
        return id == tube.id && capacity == tube.capacity && Objects.equals(balls, tube.balls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, capacity, balls);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tube[id=").append(id)
                .append(", capacity=").append(capacity)
                .append(", size=").append(getCurrentSize())
                .append(", balls=[");

        for (int i = 0; i < balls.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(balls.get(i).getColor());
        }
        sb.append("]]");
        return sb.toString();
    }
}