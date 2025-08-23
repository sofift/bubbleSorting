package it.unical.informatica.asp.ai;

import it.unical.informatica.model.Move;
import java.util.*;
import java.util.regex.*;

public final class AspParsers {
    private AspParsers() {}

    // move(F,T,S)
    private static final Pattern MOVE_RX = Pattern.compile("\\bmove\\((\\d+),(\\d+),(\\d+)\\)");

    public static boolean parseCheckReached(String solverOutput) {
        // nel check.lp forziamo 'reached' se il goal è raggiungibile
        return solverOutput != null && solverOutput.contains("reached");
    }

    public static List<Move> parsePlan(String solverOutput) {
        if (solverOutput == null || solverOutput.isBlank()) return List.of();

        class Tmp { final int f,t,s; Tmp(int f,int t,int s){this.f=f;this.t=t;this.s=s;} }

        List<Tmp> tmp = new ArrayList<>();
        Matcher m = MOVE_RX.matcher(solverOutput);
        while (m.find()) {
            int from = Integer.parseInt(m.group(1));
            int to   = Integer.parseInt(m.group(2));
            int step = Integer.parseInt(m.group(3));
            tmp.add(new Tmp(from, to, step));
        }

        // ordina per step crescente
        tmp.sort(Comparator.comparingInt(x -> x.s));

        // mappa a Move(from,to) — il tuo costruttore reale
        List<Move> plan = new ArrayList<>(tmp.size());
        for (Tmp x : tmp) plan.add(new Move(x.f, x.t));

        return plan;
    }
}
