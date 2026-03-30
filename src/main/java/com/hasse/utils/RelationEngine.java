package com.hasse.utils;

import com.hasse.model.AppState;

import java.util.*;
import java.util.stream.Collectors;

public class RelationEngine {

    public static void computeRelations(AppState state) {
        List<String> elements = state.getElements();
        Set<String[]> relations = new LinkedHashSet<>();
        String type = state.getRelationType();

        switch (type) {
            case "divides":
                for (String a : elements) {
                    for (String b : elements) {
                        try {
                            int ia = Integer.parseInt(a.trim());
                            int ib = Integer.parseInt(b.trim());
                            if (ia != 0 && ib % ia == 0) relations.add(new String[]{a.trim(), b.trim()});
                        } catch (NumberFormatException ignored) {}
                    }
                }
                break;
            case "lessthan":
                for (String a : elements) {
                    for (String b : elements) {
                        try {
                            int ia = Integer.parseInt(a.trim());
                            int ib = Integer.parseInt(b.trim());
                            if (ia <= ib) relations.add(new String[]{a.trim(), b.trim()});
                        } catch (NumberFormatException e) {
                            if (a.trim().compareTo(b.trim()) <= 0) relations.add(new String[]{a.trim(), b.trim()});
                        }
                    }
                }
                break;
            default:
                break;
        }

        state.setRelations(relations);
        analyzeProperties(state);
    }

    public static void analyzeProperties(AppState state) {
        List<String> elements = state.getElements();
        Set<String[]> rels = state.getRelations();
        Set<String> relSet = rels.stream().map(r -> r[0] + "," + r[1]).collect(Collectors.toSet());

        boolean reflexive = elements.stream().allMatch(e -> relSet.contains(e + "," + e));
        boolean symmetric = rels.stream().allMatch(r -> relSet.contains(r[1] + "," + r[0]));
        boolean antisymmetric = rels.stream().allMatch(r -> !relSet.contains(r[1] + "," + r[0]) || r[0].equals(r[1]));
        boolean transitive = true;
        outer:
        for (String[] r1 : rels) {
            for (String[] r2 : rels) {
                if (r1[1].equals(r2[0])) {
                    if (!relSet.contains(r1[0] + "," + r2[1])) { transitive = false; break outer; }
                }
            }
        }

        state.setReflexive(reflexive);
        state.setSymmetric(symmetric);
        state.setAntisymmetric(antisymmetric);
        state.setTransitive(transitive);
    }

    public static void computeTransitiveReduction(AppState state) {
        Set<String[]> rels = state.getRelations();
        List<String> elements = state.getElements();
        List<String[]> allPairs = new ArrayList<>();
        for (String[] r : rels) {
            if (!r[0].equals(r[1])) allPairs.add(r);
        }

        Set<String> toRemove = new HashSet<>();
        List<String> steps = new ArrayList<>();

        for (String[] r : allPairs) {
            String a = r[0], b = r[1];
            if (hasIndirectPath(a, b, a, allPairs, elements, new HashSet<>(), toRemove)) {
                toRemove.add(a + "," + b);
                steps.add("(" + a + "," + b + ") removed — path " + a + " → ... → " + b + " exists");
            }
        }

        state.setReductionSteps(steps);
        Set<String[]> reduced = new LinkedHashSet<>();
        for (String[] r : allPairs) {
            if (!toRemove.contains(r[0] + "," + r[1])) reduced.add(r);
        }
        state.setReducedRelations(reduced);
    }

    private static boolean hasIndirectPath(String start, String target, String current,
                                            List<String[]> allPairs, List<String> elements,
                                            Set<String> visited, Set<String> toRemove) {
        for (String[] r : allPairs) {
            if (r[0].equals(current) && !visited.contains(r[1]) && !r[1].equals(start)) {
                if (r[1].equals(target) && !current.equals(start)) return true;
                if (!r[1].equals(target)) {
                    Set<String> newVisited = new HashSet<>(visited);
                    newVisited.add(current);
                    if (hasIndirectPath(start, target, r[1], allPairs, elements, newVisited, toRemove)) return true;
                }
            }
        }
        return false;
    }

    public static Set<String[]> parseCustomRelations(String input, List<String> elements) {
        Set<String[]> result = new LinkedHashSet<>();
        input = input.replaceAll("\\s+", "");
        String[] pairs = input.split("\\),\\(|\\(|\\)");
        for (String pair : pairs) {
            if (pair.trim().isEmpty()) continue;
            String[] parts = pair.split(",");
            if (parts.length == 2) result.add(new String[]{parts[0].trim(), parts[1].trim()});
        }
        return result;
    }
}
