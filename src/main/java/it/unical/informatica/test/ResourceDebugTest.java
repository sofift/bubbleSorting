package it.unical.informatica.test;

import java.io.InputStream;
import java.net.URL;

/**
 * Debug per verificare il caricamento dei file ASP
 */
public class ResourceDebugTest {

    public static void main(String[] args) {
        System.out.println("=== DEBUG CARICAMENTO RISORSE ASP ===\n");

        ResourceDebugTest test = new ResourceDebugTest();
        test.testResourceLoading();
    }

    private void testResourceLoading() {
        String[] files = {
                "bubble_solver.asp",
                "bubble_hint.asp",
                "bubble_simple.asp",
                "bubble_check.asp"
        };

        for (String fileName : files) {
            testFile(fileName);
        }

        // Test diversi path
        System.out.println("\n=== TEST PATH ALTERNATIVI ===");
        testAlternativePaths("bubble_solver.asp");
    }

    private void testFile(String fileName) {
        System.out.println("Test: " + fileName);

        // Metodo 1: Senza / iniziale
        String path1 = "asp/" + fileName;
        testPath(path1, "Metodo 1");

        // Metodo 2: Con / iniziale
        String path2 = "/asp/" + fileName;
        testPath(path2, "Metodo 2");

        System.out.println();
    }

    private void testPath(String path, String metodo) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream != null) {
                int size = inputStream.available();
                System.out.println("  ✅ " + metodo + " (" + path + "): " + size + " bytes");
            } else {
                System.out.println("  ❌ " + metodo + " (" + path + "): NULL");
            }
        } catch (Exception e) {
            System.out.println("  ❌ " + metodo + " (" + path + "): " + e.getMessage());
        }
    }

    private void testAlternativePaths(String fileName) {
        String[] paths = {
                "asp/" + fileName,
                "/asp/" + fileName,
                fileName,
                "/" + fileName,
                "src/main/resources/asp/" + fileName,
                "resources/asp/" + fileName
        };

        for (String path : paths) {
            testSinglePath(path);
        }

        // Test URL
        testUrl("asp/" + fileName);
    }

    private void testSinglePath(String path) {
        try (InputStream inputStream = ResourceDebugTest.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream != null) {
                System.out.println("  ✅ Trovato: " + path);
            } else {
                System.out.println("  ❌ Non trovato: " + path);
            }
        } catch (Exception e) {
            System.out.println("  ❌ Errore " + path + ": " + e.getMessage());
        }
    }

    private void testUrl(String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                System.out.println("  ✅ URL trovato: " + url);
            } else {
                System.out.println("  ❌ URL non trovato per: " + path);
            }
        } catch (Exception e) {
            System.out.println("  ❌ Errore URL " + path + ": " + e.getMessage());
        }
    }
}