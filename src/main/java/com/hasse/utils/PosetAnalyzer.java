package com.hasse.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PosetAnalyzer
 *
 * Aa class no main kaam che:
 * - Poset (Partially Ordered Set) analyze karvu
 * - Minimal / Maximal elements find karva
 * - Least / Greatest element check karvu
 * - Lattice che ke nai e check karvu
 * - Meet ane Join table generate karvi
 *
 * Simple ma:
 * Input → Elements + Cover Edges
 * Output → Full analysis of poset
 */
public class PosetAnalyzer {

    // Badha elements (nodes) store kare che
    private final List<String> elementsList;

    // Hasse diagram ma je direct edges hoy (cover relation)
    private final List<String[]> coverRelations;

    // "belowMap" → koi element ni niche kaya elements che
    private final Map<String, Set<String>> belowMap = new HashMap<>();

    // "aboveMap" → koi element ni upar kaya elements che
    private final Map<String, Set<String>> aboveMap = new HashMap<>();

    // Result variables
    private List<String> minimalElements;
    private List<String> maximalElements;
    private String leastElement;
    private String greatestElement;
    private boolean latticeFlag;

    // Meet ane Join tables (2D structure)
    private Map<String, Map<String, String>> meetTable = new LinkedHashMap<>();
    private Map<String, Map<String, String>> joinTable = new LinkedHashMap<>();

    /**
     * Constructor
     * Aa jagya thi pura analysis ni process start thay che
     */
    public PosetAnalyzer(List<String> elements, List<String[]> coverEdges) {
        this.elementsList = new ArrayList<>(elements);
        this.coverRelations = new ArrayList<>(coverEdges);

        buildTransitiveClosure(); // Step 1
        analyzePoset();           // Step 2
    }

    /**
     * Step 1: Transitive Closure banavvu
     *
     * Aa function find kare che:
     * - Kaya element ni upar kaya elements che
     * - Kaya element ni niche kaya elements che
     */
    private void buildTransitiveClosure() {

        // Successor → direct upar na nodes
        Map<String, Set<String>> successorMap = new HashMap<>();

        // Predecessor → direct niche na nodes
        Map<String, Set<String>> predecessorMap = new HashMap<>();

        // Initialize maps
        for (String element : elementsList) {
            successorMap.put(element, new HashSet<>());
            predecessorMap.put(element, new HashSet<>());
        }

        // Cover edges thi relation build karo
        for (String[] edge : coverRelations) {
            successorMap.get(edge[0]).add(edge[1]);
            predecessorMap.get(edge[1]).add(edge[0]);
        }

        // DFS thi badha reachable nodes find karo (upar)
        for (String element : elementsList) {
            Set<String> reachableAbove = new HashSet<>();
            depthFirstSearch(element, successorMap, reachableAbove);
            aboveMap.put(element, reachableAbove);
        }

        // DFS thi badha reachable nodes find karo (niche)
        for (String element : elementsList) {
            Set<String> reachableBelow = new HashSet<>();
            depthFirstSearch(element, predecessorMap, reachableBelow);
            belowMap.put(element, reachableBelow);
        }
    }

    /**
     * DFS (Depth First Search)
     *
     * Recursive function che je graph traverse kare che
     */
    private void depthFirstSearch(String current,
                                  Map<String, Set<String>> adjacencyMap,
                                  Set<String> visitedSet) {

        visitedSet.add(current);

        for (String next : adjacencyMap.getOrDefault(current, Collections.emptySet())) {
            if (!visitedSet.contains(next)) {
                depthFirstSearch(next, adjacencyMap, visitedSet);
            }
        }
    }

    /**
     * Step 2: Main Analysis
     *
     * Aa function badha important result calculate kare che
     */
    private void analyzePoset() {

        // In-degree ane Out-degree calculate karo
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Integer> outDegree = new HashMap<>();

        for (String element : elementsList) {
            inDegree.put(element, 0);
            outDegree.put(element, 0);
        }

        for (String[] edge : coverRelations) {
            outDegree.merge(edge[0], 1, Integer::sum);
            inDegree.merge(edge[1], 1, Integer::sum);
        }

        // Minimal elements → jena upar koi nai hoy (inDegree = 0)
        minimalElements = elementsList.stream()
                .filter(e -> inDegree.get(e) == 0)
                .sorted()
                .collect(Collectors.toList());

        // Maximal elements → jena niche koi nai hoy (outDegree = 0)
        maximalElements = elementsList.stream()
                .filter(e -> outDegree.get(e) == 0)
                .sorted()
                .collect(Collectors.toList());

        /**
         * Least Element:
         * Je badha elements karta niche hoy
         */
        leastElement = null;
        for (String candidate : minimalElements) {
            if (elementsList.stream().allMatch(e -> aboveMap.get(candidate).contains(e))) {
                leastElement = candidate;
                break;
            }
        }

        /**
         * Greatest Element:
         * Je badha elements karta upar hoy
         */
        greatestElement = null;
        for (String candidate : maximalElements) {
            if (elementsList.stream().allMatch(e -> belowMap.get(candidate).contains(e))) {
                greatestElement = candidate;
                break;
            }
        }

        /**
         * Lattice Check:
         * Badha pairs mate meet ane join exist kare to lattice
         */
        latticeFlag = true;

        // Table initialize karo
        for (String a : elementsList) {
            meetTable.put(a, new LinkedHashMap<>());
            joinTable.put(a, new LinkedHashMap<>());
        }

        // Pair-wise computation
        for (String a : elementsList) {
            for (String b : elementsList) {

                String meetValue = findMeet(a, b);
                String joinValue = findJoin(a, b);

                meetTable.get(a).put(b, meetValue != null ? meetValue : "—");
                joinTable.get(a).put(b, joinValue != null ? joinValue : "—");

                if (meetValue == null || joinValue == null) {
                    latticeFlag = false;
                }
            }
        }
    }

    /**
     * Meet (Greatest Lower Bound)
     *
     * a ane b banne karta niche je greatest element hoy
     */
    private String findMeet(String a, String b) {

        Set<String> commonBelow = new HashSet<>(belowMap.get(a));
        commonBelow.retainAll(belowMap.get(b));

        if (commonBelow.isEmpty()) return null;

        for (String candidate : commonBelow) {
            if (commonBelow.stream().allMatch(other -> aboveMap.get(candidate).contains(other))) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Join (Least Upper Bound)
     *
     * a ane b banne karta upar je smallest element hoy
     */
    private String findJoin(String a, String b) {

        Set<String> commonAbove = new HashSet<>(aboveMap.get(a));
        commonAbove.retainAll(aboveMap.get(b));

        if (commonAbove.isEmpty()) return null;

        for (String candidate : commonAbove) {
            if (commonAbove.stream().allMatch(other -> belowMap.get(candidate).contains(other))) {
                return candidate;
            }
        }

        return null;
    }

    // ===== Getters (simple access methods) =====

    public List<String> getElements() { return elementsList; }

    public List<String> getMinimalElements() { return minimalElements; }

    public List<String> getMaximalElements() { return maximalElements; }

    public String getLeastElement() { return leastElement; }

    public String getGreatestElement() { return greatestElement; }

    public boolean isLattice() { return latticeFlag; }

    public Map<String, Map<String, String>> getMeetTable() { return meetTable; }

    public Map<String, Map<String, String>> getJoinTable() { return joinTable; }
}