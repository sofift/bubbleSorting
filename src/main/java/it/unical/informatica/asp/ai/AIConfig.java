// src/main/java/it/unical/informatica/ai/AIConfig.java
package it.unical.informatica.asp.ai;

public final class AIConfig {
    private AIConfig() {}

    // Solver: metti il path SENZA spazi/parentesi. Esempio: "libs/dlv2.exe"
    public static final String DLV2_PATH = "libs/dlv2.exe";

    // File regole (li useremo tra poco; per adesso puoi lasciarli così)
    public static final String CHECK_RULES = "src/main/resources/asp/check.lp";
    public static final String SOLVE_RULES_DLV2 = "src/main/resources/asp/solve_dlv2.lp";

    // Orizzonte massimo passi di piano
    public static final int MAX_HORIZON = 40;

    // Timeout (secondi) per ciascuna chiamata ASP
    public static final int ASP_TIMEOUT_SECONDS = 30;

    // Massimo tentativi di rigenerazione di un livello random se non risolvibile
    public static final int MAX_GENERATION_TRIES = 25;
}
