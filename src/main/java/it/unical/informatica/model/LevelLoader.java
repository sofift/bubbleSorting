package it.unical.informatica.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Classe responsabile del caricamento dei livelli dal file JSON.
 * VERSIONE DEBUG con logging esteso per identificare problemi.
 */
public class LevelLoader {
    private static final String LEVELS_FILE = "/levels/levels.json";
    private JSONObject levelsData;

    public LevelLoader() {
        loadLevelsFile();
    }

    /**
     * Carica il file JSON con tutti i livelli
     */
    private void loadLevelsFile() {
        System.out.println("📁 Tentativo di caricamento file: " + LEVELS_FILE);

        try (InputStream inputStream = getClass().getResourceAsStream(LEVELS_FILE)) {
            if (inputStream == null) {
                System.err.println("❌ File non trovato: " + LEVELS_FILE);
                System.err.println("🔍 Percorsi alternativi da controllare:");
                System.err.println("   - src/main/resources/levels/levels.json");
                System.err.println("   - target/classes/levels/levels.json");
                throw new FileNotFoundException("File dei livelli non trovato: " + LEVELS_FILE);
            }

            String jsonContent = readInputStream(inputStream);
            System.out.println("✅ File letto, dimensione: " + jsonContent.length() + " caratteri");
            System.out.println("🔍 Primi 200 caratteri: " + jsonContent.substring(0, Math.min(200, jsonContent.length())));

            this.levelsData = new JSONObject(jsonContent);
            System.out.println("✅ JSON parsato con successo");

            // Verifica la struttura
            validateJSONStructure();

        } catch (IOException e) {
            System.err.println("❌ Errore I/O nel caricamento del file: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("❌ Errore nel parsing JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Errore generico nel caricamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida la struttura del JSON
     */
    private void validateJSONStructure() {
        try {
            if (!levelsData.has("levels")) {
                throw new JSONException("Chiave 'levels' mancante nel JSON");
            }

            JSONObject levels = levelsData.getJSONObject("levels");
            System.out.println("🔍 Difficoltà trovate nel JSON: " + levels.keySet());

            // Verifica ogni difficoltà
            for (GameLevel difficulty : GameLevel.values()) {
                String difficultyName = difficulty.name();
                if (levels.has(difficultyName)) {
                    JSONArray difficultyLevels = levels.getJSONArray(difficultyName);
                    System.out.println("   " + difficultyName + ": " + difficultyLevels.length() + " livelli");
                } else {
                    System.err.println("❌ Difficoltà mancante: " + difficultyName);
                }
            }

        } catch (JSONException e) {
            System.err.println("❌ Struttura JSON non valida: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Legge il contenuto di un InputStream
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    /**
     * Carica un livello specifico nello stato del gioco
     * @param gameState Stato del gioco da configurare
     * @param difficulty Difficoltà del livello
     * @param levelNumber Numero del livello (1-5)
     */
    public void loadLevel(GameState gameState, GameLevel difficulty, int levelNumber)
            throws LevelLoadException {

        System.out.println("🎯 Caricamento livello: " + difficulty.getDisplayName() + " - " + levelNumber);

        if (levelsData == null) {
            throw new LevelLoadException("File JSON non caricato correttamente");
        }

        if (!difficulty.isValidLevelNumber(levelNumber)) {
            throw new LevelLoadException("Numero livello non valido: " + levelNumber +
                    " per difficoltà " + difficulty);
        }

        try {
            JSONObject levels = levelsData.getJSONObject("levels");

            if (!levels.has(difficulty.name())) {
                throw new LevelLoadException("Difficoltà non trovata nel JSON: " + difficulty.name());
            }

            JSONArray difficultyLevels = levels.getJSONArray(difficulty.name());
            System.out.println("🔍 Trovati " + difficultyLevels.length() + " livelli per " + difficulty.name());

            if (levelNumber - 1 >= difficultyLevels.length()) {
                throw new LevelLoadException("Livello non trovato: " + levelNumber +
                        " per difficoltà " + difficulty + " (disponibili: " + difficultyLevels.length() + ")");
            }

            JSONObject levelData = difficultyLevels.getJSONObject(levelNumber - 1);
            System.out.println("✅ Dati livello trovati: " + levelData.toString());

            loadLevelData(gameState, levelData, difficulty, levelNumber);

        } catch (JSONException e) {
            System.err.println("❌ Errore JSON nella lettura del livello: " + e.getMessage());
            throw new LevelLoadException("Errore nella lettura del livello: " + e.getMessage(), e);
        }
    }

    /**
     * Carica i dati di un livello specifico nei tubi del gioco
     */
    private void loadLevelData(GameState gameState, JSONObject levelData, GameLevel difficulty, int levelNumber) throws JSONException {
        System.out.println("🔧 Caricamento dati nel GameState...");

        if (!levelData.has("tubes")) {
            throw new JSONException("Chiave 'tubes' mancante nei dati del livello");
        }

        JSONArray tubesData = levelData.getJSONArray("tubes");
        List<Tube> tubes = gameState.getTubes();

        System.out.println("🔍 Tubi nel JSON: " + tubesData.length() + ", Tubi nel GameState: " + tubes.size());

        if (tubesData.length() != tubes.size()) {
            throw new JSONException("Numero di tubi non corrisponde: atteso " +
                    tubes.size() + ", trovato " + tubesData.length());
        }

        for (int tubeIndex = 0; tubeIndex < tubesData.length(); tubeIndex++) {
            Tube tube = tubes.get(tubeIndex);
            tube.clear(); // Pulisce il tubo prima di riempirlo

            JSONArray ballsData = tubesData.getJSONArray(tubeIndex);
            System.out.println("   Tubo " + (tubeIndex + 1) + ": " + ballsData.length() + " palline");

            for (int ballIndex = 0; ballIndex < ballsData.length(); ballIndex++) {
                String colorName = ballsData.getString(ballIndex);
                System.out.println("      Pallina " + ballIndex + ": " + colorName);

                try {
                    BallColor color = BallColor.fromString(colorName);
                    Ball ball = new Ball(color);

                    // ✅ USA addBallForLoading() per bypassare le regole del gioco durante il caricamento
                    if (!tube.addBallForLoading(ball)) {
                        throw new JSONException("Impossibile aggiungere pallina " + colorName +
                                " al tubo " + (tubeIndex + 1) + " alla posizione " + ballIndex +
                                " (tubo pieno o errore)");
                    }

                } catch (IllegalArgumentException e) {
                    System.err.println("❌ Colore non valido: " + colorName);
                    throw new JSONException("Colore non valido: " + colorName);
                }
            }
        }

        System.out.println("✅ Livello caricato con successo!");

        // Debug: stampa la configurazione finale
        printLoadedConfiguration(gameState);
    }

    /**
     * Stampa la configurazione caricata per debug
     */
    private void printLoadedConfiguration(GameState gameState) {
        System.out.println("🎯 CONFIGURAZIONE CARICATA:");
        List<Tube> tubes = gameState.getTubes();
        for (int i = 0; i < tubes.size(); i++) {
            Tube tube = tubes.get(i);
            List<Ball> balls = tube.getBalls();

            System.out.print("   Tubo " + (i + 1) + ": [");
            if (balls.isEmpty()) {
                System.out.print("vuoto");
            } else {
                for (int j = 0; j < balls.size(); j++) {
                    if (j > 0) System.out.print(", ");
                    System.out.print(balls.get(j).getColor().name());
                }
            }
            System.out.println("]");
        }
    }

    /**
     * Ottiene informazioni su un livello senza caricarlo
     */
    public LevelInfo getLevelInfo(GameLevel difficulty, int levelNumber) {
        if (levelsData == null) {
            System.err.println("❌ levelsData è null, impossibile ottenere info livello");
            return null;
        }

        try {
            JSONObject levels = levelsData.getJSONObject("levels");
            JSONArray difficultyLevels = levels.getJSONArray(difficulty.name());

            if (levelNumber - 1 < difficultyLevels.length()) {
                JSONObject levelData = difficultyLevels.getJSONObject(levelNumber - 1);
                return new LevelInfo(difficulty, levelNumber, levelData);
            }

        } catch (JSONException e) {
            System.err.println("❌ Errore nel recupero informazioni livello: " + e.getMessage());
        }

        return null;
    }

    /**
     * Controlla se un livello esiste
     */
    public boolean levelExists(GameLevel difficulty, int levelNumber) {
        boolean exists = getLevelInfo(difficulty, levelNumber) != null;
        System.out.println("🔍 Livello " + difficulty.name() + " " + levelNumber +
                " esiste: " + exists);
        return exists;
    }

    /**
     * Ottiene il numero di livelli disponibili per una difficoltà
     */
    public int getAvailableLevels(GameLevel difficulty) {
        if (levelsData == null) {
            return difficulty.getMaxLevels(); // Ritorna il default
        }

        try {
            JSONObject levels = levelsData.getJSONObject("levels");
            JSONArray difficultyLevels = levels.getJSONArray(difficulty.name());
            return difficultyLevels.length();

        } catch (JSONException e) {
            System.err.println("❌ Errore nel conteggio livelli: " + e.getMessage());
            return difficulty.getMaxLevels(); // Ritorna il default
        }
    }

    /**
     * Verifica se il file JSON è stato caricato correttamente
     */
    public boolean isLoaded() {
        return levelsData != null;
    }

    /**
     * Classe per le eccezioni di caricamento livelli
     */
    public static class LevelLoadException extends Exception {
        public LevelLoadException(String message) {
            super(message);
        }

        public LevelLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Classe per contenere informazioni su un livello
     */
    public static class LevelInfo {
        private final GameLevel difficulty;
        private final int levelNumber;
        private final int tubesCount;
        private final int ballsCount;

        public LevelInfo(GameLevel difficulty, int levelNumber, JSONObject levelData) {
            this.difficulty = difficulty;
            this.levelNumber = levelNumber;

            try {
                JSONArray tubes = levelData.getJSONArray("tubes");
                this.tubesCount = tubes.length();

                int totalBalls = 0;
                for (int i = 0; i < tubes.length(); i++) {
                    totalBalls += tubes.getJSONArray(i).length();
                }
                this.ballsCount = totalBalls;

            } catch (JSONException e) {
                throw new RuntimeException("Errore nel parsing delle informazioni livello", e);
            }
        }

        // Getters
        public GameLevel getDifficulty() { return difficulty; }
        public int getLevelNumber() { return levelNumber; }
        public int getTubesCount() { return tubesCount; }
        public int getBallsCount() { return ballsCount; }

        @Override
        public String toString() {
            return String.format("Level %d (%s): %d tubi, %d palline",
                    levelNumber, difficulty.getDisplayName(),
                    tubesCount, ballsCount);
        }
    }
}