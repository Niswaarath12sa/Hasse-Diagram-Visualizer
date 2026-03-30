package com.hasse.utils;

import java.util.*;
import java.util.stream.Collectors;

public class PosetAnalyzer {

    private final List<String> elements;
    private final List<String[]> coverEdges;
    private final Map<String, Set<String>> below = new HashMap<>();
    private final Map<String, Set<String>> above = new HashMap<>();

    private List<String> minimalElements;
    private List<String> maximalElements;
    private String leastElement;
    private String greatestElement;
    private boolean isLattice;
    private Map<String, Map<String, String>> meetTable = new LinkedHashMap<>();
    private Map<String, Map<String, String>> joinTable = new LinkedHashMap<>();

    public PosetAnalyzer(List<String> elements, List<String[]> coverEdges) {
        this.elements = new ArrayList<>(elements);
        this.coverEdges = new ArrayList<>(coverEdges);
        computeClosure();
        analyze();
    }

    private void computeClosure() {
        Map<String, Set<String>> succ = new HashMap<>();
        Map<String, Set<String>> pred = new HashMap<>();
        for (String e : elements) { succ.put(e, new HashSet<>()); pred.put(e, new HashSet<>()); }
        for (String[] edge : coverEdges) { succ.get(edge[0]).add(edge[1]); pred.get(edge[1]).add(edge[0]); }
        for (String x : elements) { Set<String> r = new HashSet<>(); dfs(x, succ, r); above.put(x, r); }
        for (String x : elements) { Set<String> r = new HashSet<>(); dfs(x, pred, r); below.put(x, r); }
    }

    private void dfs(String start, Map<String, Set<String>> adj, Set<String> visited) {
        visited.add(start);
        for (String next : adj.getOrDefault(start, Collections.emptySet()))
            if (!visited.contains(next)) dfs(next, adj, visited);
    }

    private void analyze() {
        Map<String, Integer> inDeg = new HashMap<>(), outDeg = new HashMap<>();
        for (String e : elements) { inDeg.put(e, 0); outDeg.put(e, 0); }
        for (String[] edge : coverEdges) { outDeg.merge(edge[0], 1, Integer::sum); inDeg.merge(edge[1], 1, Integer::sum); }

        minimalElements = elements.stream().filter(e -> inDeg.get(e) == 0).sorted().collect(Collectors.toList());
        maximalElements = elements.stream().filter(e -> outDeg.get(e) == 0).sorted().collect(Collectors.toList());

        leastElement = null;
        for (String c : minimalElements)
            if (elements.stream().allMatch(e -> above.get(c).contains(e))) { leastElement = c; break; }

        greatestElement = null;
        for (String c : maximalElements)
            if (elements.stream().allMatch(e -> below.get(c).contains(e))) { greatestElement = c; break; }

        isLattice = true;
        for (String a : elements) { meetTable.put(a, new LinkedHashMap<>()); joinTable.put(a, new LinkedHashMap<>()); }
        for (String a : elements) {
            for (String b : elements) {
                String meet = computeMeet(a, b);
                String join = computeJoin(a, b);
                meetTable.get(a).put(b, meet != null ? meet : "—");
                joinTable.get(a).put(b, join != null ? join : "—");
                if (meet == null || join == null) isLattice = false;
            }
        }
    }

    private String computeMeet(String a, String b) {
        Set<String> common = new HashSet<>(below.get(a));
        common.retainAll(below.get(b));
        if (common.isEmpty()) return null;
        for (String c : common)
            if (common.stream().allMatch(o -> above.get(c).contains(o))) return c;
        return null;
    }

    private String computeJoin(String a, String b) {
        Set<String> common = new HashSet<>(above.get(a));
        common.retainAll(above.get(b));
        if (common.isEmpty()) return null;
        for (String c : common)
            if (common.stream().allMatch(o -> below.get(c).contains(o))) return c;
        return null;
    }

    public List<String> getElements() { return elements; }
    public List<String> getMinimalElements() { return minimalElements; }
    public List<String> getMaximalElements() { return maximalElements; }
    public String getLeastElement() { return leastElement; }
    public String getGreatestElement() { return greatestElement; }
    public boolean isLattice() { return isLattice; }
    public Map<String, Map<String, String>> getMeetTable() { return meetTable; }
    public Map<String, Map<String, String>> getJoinTable() { return joinTable; }
}
