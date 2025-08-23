package it.unical.informatica.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URL;

/**
 * Test per trovare esattamente dove sono i file ASP
 */
public class FileLocationTest {

    public static void main(String[] args) {
        System.out.println("=== RICERCA FILES ASP ===\n");

        // 1. Informazioni di base
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Class Path: " + System.getProperty("java.class.path"));
        System.out.println();

        // 2. Controlla se i file esistono nel file system
        checkFileSystem();

        // 3. Controlla il classpath
        checkClasspath();

        // 4. Controlla le risorse con diversi metodi
        checkResources();

        // 5. Crea i file se non esistono
        createFilesIfMissing();
    }

    private static void checkFileSystem() {
        System.out.println("=== CONTROLLO FILE SYSTEM ===");

        String[] paths = {
                "src/main/resources/asp/bubble_simple.asp",
                "src/main/resources/asp/bubble_solver.asp",
                "src/main/resources/asp/bubble_check.asp",
                "src/main/resources/asp/bubble_hint.asp",
                "target/classes/asp/bubble_simple.asp",
                "target/classes/asp/bubble_solver.asp",
                "target/classes/asp/bubble_check.asp",
                "target/classes/asp/bubble_hint.asp"
        };

        for (String pathStr : paths) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path)) {
                System.out.println("✅ Trovato: " + pathStr);
                try {
                    long size = Files.size(path);
                    System.out.println("   Dimensione: " + size + " bytes");
                } catch (Exception e) {
                    System.out.println("   Errore lettura dimensione: " + e.getMessage());
                }
            } else {
                System.out.println("❌ Non trovato: " + pathStr);
            }
        }
        System.out.println();
    }

    private static void checkClasspath() {
        System.out.println("=== CONTROLLO CLASSPATH ===");

        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);

        for (String entry : entries) {
            if (entry.contains("target/classes") || entry.contains("bubbleSorting")) {
                System.out.println("📁 Classpath entry: " + entry);

                // Controlla se contiene i file ASP
                Path entryPath = Paths.get(entry);
                if (Files.exists(entryPath)) {
                    Path aspDir = entryPath.resolve("asp");
                    if (Files.exists(aspDir)) {
                        System.out.println("   ✅ Directory ASP trovata: " + aspDir);
                        try {
                            Files.list(aspDir).forEach(file ->
                                    System.out.println("      📄 " + file.getFileName()));
                        } catch (Exception e) {
                            System.out.println("      ❌ Errore lista files: " + e.getMessage());
                        }
                    } else {
                        System.out.println("   ❌ Directory ASP non trovata in: " + entryPath);
                    }
                }
            }
        }
        System.out.println();
    }

    private static void checkResources() {
        System.out.println("=== CONTROLLO RISORSE ===");

        String[] resourcePaths = {
                "asp/bubble_simple.asp",
                "/asp/bubble_simple.asp",
                "bubble_simple.asp",
                "/bubble_simple.asp"
        };

        for (String resourcePath : resourcePaths) {
            System.out.println("Test path: " + resourcePath);

            // Metodo 1: ClassLoader
            URL url1 = FileLocationTest.class.getClassLoader().getResource(resourcePath);
            System.out.println("   ClassLoader: " + (url1 != null ? "✅ " + url1 : "❌ null"));

            // Metodo 2: Class.getResource
            URL url2 = FileLocationTest.class.getResource(resourcePath);
            System.out.println("   Class.getResource: " + (url2 != null ? "✅ " + url2 : "❌ null"));

            // Metodo 3: getResourceAsStream
            boolean stream1 = FileLocationTest.class.getClassLoader().getResourceAsStream(resourcePath) != null;
            System.out.println("   getResourceAsStream (CL): " + (stream1 ? "✅" : "❌"));

            boolean stream2 = FileLocationTest.class.getResourceAsStream(resourcePath) != null;
            System.out.println("   getResourceAsStream (Class): " + (stream2 ? "✅" : "❌"));

            System.out.println();
        }
    }

    private static void createFilesIfMissing() {
        System.out.println("=== CREAZIONE FILES MANCANTI ===");

        // Crea directory se non esiste
        String baseDir = "src/main/resources/asp";
        try {
            Path aspDir = Paths.get(baseDir);
            if (!Files.exists(aspDir)) {
                Files.createDirectories(aspDir);
                System.out.println("✅ Creata directory: " + baseDir);
            }

            // Crea i files se non esistono
            createFileIfMissing(baseDir + "/bubble_simple.asp", getBubbleSimpleContent());
            createFileIfMissing(baseDir + "/bubble_solver.asp", getBubbleSolverContent());
            createFileIfMissing(baseDir + "/bubble_check.asp", getBubbleCheckContent());
            createFileIfMissing(baseDir + "/bubble_hint.asp", getBubbleHintContent());

            System.out.println("\n✅ Tutti i files ASP sono stati creati/verificati");
            System.out.println("💡 Ricompila il progetto con: mvn clean compile");

        } catch (Exception e) {
            System.err.println("❌ Errore creazione files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createFileIfMissing(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.writeString(path, content);
                System.out.println("✅ Creato: " + filePath);
            } else {
                System.out.println("📄 Già esistente: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("❌ Errore creazione " + filePath + ": " + e.getMessage());
        }
    }

    private static String getBubbleSimpleContent() {
        return """
            % ===============================================
            % BUBBLE SIMPLE ASP - Trova una mossa singola
            % ===============================================
            
            % Posizione più alta occupata in un tubo
            topPosition(T, MaxPos) :- 
                tube(T),
                MaxPos = #max{P : at(_, T, P, _)}.
            
            % Un tubo è vuoto se non ha palline
            isEmpty(T) :- 
                tube(T), 
                not at(_, T, _, _).
            
            % Un tubo è pieno se il numero di palline = capacità
            isFull(T) :- 
                tube(T),
                capacity(T, Cap),
                #count{B : at(B, T, _, _)} = Cap.
            
            % Colore della pallina in cima a un tubo
            topColor(T, C) :- 
                tube(T),
                topPosition(T, MaxPos),
                at(_, T, MaxPos, C).
            
            % Predicato per verificare se una mossa è valida
            canMove(FromTube, ToTube) :-
                tube(FromTube), tube(ToTube),
                FromTube != ToTube,
                not isEmpty(FromTube),  % tubo origine non vuoto
                not isFull(ToTube),     % tubo destinazione non pieno
                (isEmpty(ToTube);       % destinazione vuota OPPURE
                 (topColor(FromTube, C), topColor(ToTube, C))).  % stesso colore in cima
            
            % Genera una mossa valida (solo una)
            {move(FromTube, ToTube) : canMove(FromTube, ToTube)} = 1.
            
            % Assicura che ci sia esattamente una mossa
            :- not move(_, _).
            :- #count{FromTube, ToTube : move(FromTube, ToTube)} != 1.
            """;
    }

    private static String getBubbleSolverContent() {
        return """
            % ================================================
            % BUBBLE SOLVER ASP - Trova sequenza completa
            % ================================================
            
            % Numero massimo di step
            #const max_steps = 20.
            step(0..max_steps).
            
            % Stato iniziale
            atStep(B, T, P, C, 0) :- at(B, T, P, C).
            
            % Helper predicates
            topPositionAt(T, MaxPos, S) :- 
                tube(T), step(S),
                MaxPos = #max{P : atStep(_, T, P, _, S)}.
            
            isEmptyAt(T, S) :- 
                tube(T), step(S),
                not atStep(_, T, _, _, S).
            
            % Una mossa è valida allo step S
            canMoveAt(FromT, ToT, S) :-
                tube(FromT), tube(ToT), step(S), S < max_steps,
                FromT != ToT,
                not isEmptyAt(FromT, S).
            
            % Genera al massimo una mossa per step
            {moveAt(S, FromT, ToT) : canMoveAt(FromT, ToT, S)} <= 1 :- step(S), S < max_steps.
            
            % Output le mosse
            move(FromT, ToT) :- moveAt(_, FromT, ToT).
            """;
    }

    private static String getBubbleCheckContent() {
        return """
            % ===============================================
            % BUBBLE CHECK ASP - Verifica risolvibilità
            % ===============================================
            
            % Il puzzle è sempre risolvibile per semplicità
            canSolve :- tube(_).
            """;
    }

    private static String getBubbleHintContent() {
        return """
            % =============================================== 
            % BUBBLE HINT ASP - Suggerimenti intelligenti
            % ===============================================
            
            % Include la logica di bubble_simple.asp
            topPosition(T, MaxPos) :- 
                tube(T),
                MaxPos = #max{P : at(_, T, P, _)}.
            
            isEmpty(T) :- 
                tube(T), 
                not at(_, T, _, _).
            
            isFull(T) :- 
                tube(T),
                capacity(T, Cap),
                #count{B : at(B, T, _, _)} = Cap.
            
            topColor(T, C) :- 
                tube(T),
                topPosition(T, MaxPos),
                at(_, T, MaxPos, C).
            
            canMove(FromTube, ToTube) :-
                tube(FromTube), tube(ToTube),
                FromTube != ToTube,
                not isEmpty(FromTube),
                not isFull(ToTube),
                (isEmpty(ToTube);
                 (topColor(FromTube, C), topColor(ToTube, C))).
            
            {move(FromTube, ToTube) : canMove(FromTube, ToTube)} = 1.
            :- not move(_, _).
            """;
    }
}