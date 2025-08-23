package it.unical.informatica.asp.ai;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AspRunner {

    private final String dlv2Path;
    private final String checkRulesPath;
    private final String solveRulesPath;

    public AspRunner(String dlv2Path, String checkRulesPath, String solveRulesPath) {
        this.dlv2Path = dlv2Path;
        this.checkRulesPath = checkRulesPath;
        this.solveRulesPath = solveRulesPath;
    }

    public String runCheck(String instanceFacts, Duration timeout) throws Exception {
        return runDLV2(List.of(checkRulesPath), instanceFacts, timeout);
    }

    public String runSolve(String instanceFacts, Duration timeout) throws Exception {
        return runDLV2(List.of(solveRulesPath), instanceFacts, timeout);
    }

    private String runDLV2(List<String> ruleFiles, String instanceFacts, Duration timeout) throws Exception {
        // Scriviamo l’istanza su file temporaneo
        Path tmp = Files.createTempFile("asp_instance_", ".lp");
        Files.writeString(tmp, instanceFacts, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        // Comando DLV2: dlv2 -silent -n=1 rules.lp instance.lp
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        // Costruiamo l’arglist
        new File(dlv2Path).setExecutable(true);

        var cmd = new java.util.ArrayList<String>();
        cmd.add(dlv2Path);
        cmd.add("-silent");
        cmd.add("-n=1");
        for (String r : ruleFiles) cmd.add(r);
        cmd.add(tmp.toAbsolutePath().toString());

        pb.command(cmd);

        Process p = pb.start();

        // Leggo output in un thread separato per evitare deadlock
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thread outT = new Thread(() -> {
            try (InputStream is = p.getInputStream()) {
                is.transferTo(baos);
            } catch (IOException ignored) {}
        });
        outT.start();

        boolean finished = p.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new RuntimeException("ASP timeout (" + timeout.toSeconds() + "s)");
        }
        outT.join();

        // Pulizia file temp
        try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}

        return baos.toString(StandardCharsets.UTF_8);
    }
}
