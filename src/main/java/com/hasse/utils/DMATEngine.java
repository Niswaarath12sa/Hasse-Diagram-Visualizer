package com.hasse.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DMATEngine - All DMAT (Discrete Mathematics) concepts in one place.
 *
 * Covers:
 *   - Relation generation (divides, lessthan, custom)
 *   - Relation properties (reflexive, symmetric, antisymmetric, transitive)
 *   - Transitive reduction (Hasse form)
 *   - Minimal and maximal elements
 *   - Least and greatest elements
 *   - Meet (GLB) and Join (LUB) for every pair
 *   - Lattice detection
 *   - Meet table and Join table
 */
public class DMATEngine {

    // -------------------------------------------------------------------------
    // 1. RELATION GENERATION
    // -------------------------------------------------------------------------

    // Build "a divides b" relation from a list of integer elements
    public static Set<String[]> buildDividesRelation(List<String> elements) {
        Set<String[]> result = new LinkedHashSet<>();
        for (String a : elements) {
            for (String b : elements) {
                try {
                    int x = Integer.parseInt(a.trim());
                    int y = Integer.parseInt(b.trim());
                    if (x != 0 && y % x == 0) {
                        result.add(new String[]{a.trim(), b.trim()});
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
    }

    // Build "a <= b" relation - works for numbers and strings both
    public static Set<String[]> buildLessThanRelation(List<String> elements) {
        Set<String[]> result = new LinkedHashSet<>();
        for (String a : elements) {
            for (String b : elements) {
                try {
                    int x = Integer.parseInt(a.trim());
                    int y = Integer.parseInt(b.trim());
                    if (x <= y) result.add(new String[]{a.trim(), b.trim()});
                } catch (NumberFormatException e) {
                    if (a.trim().compareTo(b.trim()) <= 0)
                        result.add(new String[]{a.trim(), b.trim()});
                }
            }
        }
        return result;
    }

    // Parse custom relations typed by user like (1,2),(2,3)
    public static Set<String[]> parseCustomRelation(String input) {
        Set<String[]> result = new LinkedHashSet<>();
        // remove all whitespace first
        input = input.replaceAll("\\s+", "");
        // split by ),( pattern to get individual pairs
        String[] parts = input.split("\\),\\(|\\(|\\)");
        for (String part : parts) {
            if (part.trim().isEmpty()) continue;
            String[] sides = part.split(",");
            if (sides.length == 2) {
                result.add(new String[]{sides[0].trim(), sides[1].trim()});
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // 2. RELATION PROPERTIES
    // -------------------------------------------------------------------------

    // Check if every element relates to itself
    public static boolean isReflexive(List<String> elements, Set<String[]> relations) {
        Set<String> relSet = toStringSet(relations);
        for (String e : elements) {
            if (!relSet.contains(e + "," + e)) return false;
        }
        return true;
    }

    // Check if (a,b) always means (b,a) also exists
    public static boolean isSymmetric(Set<String[]> relations) {
        Set<String> relSet = toStringSet(relations);
        for (String[] r : relations) {
            if (!relSet.contains(r[1] + "," + r[0])) return false;
        }
        return true;
    }

    // Check if (a,b) and (b,a) together means a must equal b
    public static boolean isAntisymmetric(Set<String[]> relations) {
        Set<String> relSet = toStringSet(relations);
        for (String[] r : relations) {
            if (relSet.contains(r[1] + "," + r[0]) && !r[0].equals(r[1])) return false;
        }
        return true;
    }

    // Check if (a,b) and (b,c) always means (a,c) is there
    public static boolean isTransitive(Set<String[]> relations) {
        Set<String> relSet = toStringSet(relations);
        for (String[] r1 : relations) {
            for (String[] r2 : relations) {
                if (r1[1].equals(r2[0])) {
                    if (!relSet.contains(r1[0] + "," + r2[1])) return false;
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // 3. TRANSITIVE REDUCTION (Hasse form - remove redundant edges)
    // -------------------------------------------------------------------------

    public static List<String[]> computeTransitiveReduction(Set<String[]> relations, List<String> reductionSteps) {
        // only work with non-reflexive pairs
        List<String[]> pairs = relations.stream()
                .filter(r -> !r[0].equals(r[1]))
                .collect(Collectors.toList());

        Set<String> toRemove = new HashSet<>();

        for (String[] r : pairs) {
            String a = r[0], b = r[1];
            // if there is another path from a to b (not direct), this edge is redundant
            if (hasAlternatePath(a, b, a, pairs, new HashSet<>(), toRemove)) {
                toRemove.add(a + "," + b);
                if (reductionSteps != null)
                    reductionSteps.add("(" + a + "," + b + ") removed — indirect path from " + a + " → ... → " + b + " exists");
            }
        }

        List<String[]> reduced = new ArrayList<>();
        for (String[] r : pairs) {
            if (!toRemove.contains(r[0] + "," + r[1]))
                reduced.add(r);
        }
        return reduced;
    }

    private static boolean hasAlternatePath(String start, String target, String current,
                                             List<String[]> pairs, Set<String> visited, Set<String> excluded) {
        for (String[] r : pairs) {
            if (!r[0].equals(current)) continue;
            if (visited.contains(r[1])) continue;
            if (r[1].equals(start)) continue;

            if (r[1].equals(target) && !current.equals(start)) return true;

            if (!r[1].equals(target)) {
                Set<String> next = new HashSet<>(visited);
                next.add(current);
                if (hasAlternatePath(start, target, r[1], pairs, next, excluded)) return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // 4. POSET ANALYSIS - using the Hasse (cover) edges
    // -------------------------------------------------------------------------

    // Build "above" and "below" maps using DFS on cover edges
    // above.get(x) = everything that x can reach going UP (including x)
    // below.get(x) = everything that x can reach going DOWN (including x)
    public static Map<String, Set<String>> computeAbove(List<String> elements, List<String[]> coverEdges) {
        // successors: x -> direct covers above x
        Map<String, Set<String>> succ = new HashMap<>();
        for (String e : elements) succ.put(e, new HashSet<>());
        for (String[] edge : coverEdges) {
            succ.computeIfAbsent(edge[0], k -> new HashSet<>()).add(edge[1]);
        }
        Map<String, Set<String>> above = new HashMap<>();
        for (String x : elements) {
            Set<String> reachable = new HashSet<>();
            dfs(x, succ, reachable);
            above.put(x, reachable);
        }
        return above;
    }

    public static Map<String, Set<String>> computeBelow(List<String> elements, List<String[]> coverEdges) {
        // predecessors: x -> direct covers below x
        Map<String, Set<String>> pred = new HashMap<>();
        for (String e : elements) pred.put(e, new HashSet<>());
        for (String[] edge : coverEdges) {
            pred.computeIfAbsent(edge[1], k -> new HashSet<>()).add(edge[0]);
        }
        Map<String, Set<String>> below = new HashMap<>();
        for (String x : elements) {
            Set<String> reachable = new HashSet<>();
            dfs(x, pred, reachable);
            below.put(x, reachable);
        }
        return below;
    }

    private static void dfs(String node, Map<String, Set<String>> adj, Set<String> visited) {
        visited.add(node);
        for (String next : adj.getOrDefault(node, Collections.emptySet())) {
            if (!visited.contains(next)) dfs(next, adj, visited);
        }
    }

    // Minimal elements = elements with no incoming cover edges (nothing is strictly below them)
    public static List<String> findMinimalElements(List<String> elements, List<String[]> coverEdges) {
        Set<String> hasIncoming = new HashSet<>();
        for (String[] e : coverEdges) hasIncoming.add(e[1]);
        return elements.stream()
                .filter(e -> !hasIncoming.contains(e))
                .sorted()
                .collect(Collectors.toList());
    }

    // Maximal elements = elements with no outgoing cover edges (nothing is strictly above them)
    public static List<String> findMaximalElements(List<String> elements, List<String[]> coverEdges) {
        Set<String> hasOutgoing = new HashSet<>();
        for (String[] e : coverEdges) hasOutgoing.add(e[0]);
        return elements.stream()
                .filter(e -> !hasOutgoing.contains(e))
                .sorted()
                .collect(Collectors.toList());
    }

    // Least element = one element that is below ALL others
    public static String findLeastElement(List<String> elements, Map<String, Set<String>> above) {
        for (String candidate : elements) {
            // candidate must be able to reach every other element going upward
            boolean belowAll = elements.stream().allMatch(e -> above.get(candidate).contains(e));
            if (belowAll) return candidate;
        }
        return null;
    }

    // Greatest element = one element that is above ALL others
    public static String findGreatestElement(List<String> elements, Map<String, Set<String>> below) {
        for (String candidate : elements) {
            // candidate must have every other element reachable going downward
            boolean aboveAll = elements.stream().allMatch(e -> below.get(candidate).contains(e));
            if (aboveAll) return candidate;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // 5. MEET (Greatest Lower Bound) and JOIN (Least Upper Bound)
    // -------------------------------------------------------------------------

    // Meet of a and b = greatest element in (below(a) ∩ below(b))
    public static String computeMeet(String a, String b,
                                      Map<String, Set<String>> below,
                                      Map<String, Set<String>> above) {
        // common lower bounds
        Set<String> commonLower = new HashSet<>(below.get(a));
        commonLower.retainAll(below.get(b));
        if (commonLower.isEmpty()) return null;

        // find the greatest among them - it must be above all others in the set
        for (String candidate : commonLower) {
            boolean isGreatest = commonLower.stream()
                    .allMatch(other -> above.get(candidate).contains(other));
            if (isGreatest) return candidate;
        }
        return null;
    }

    // Join of a and b = least element in (above(a) ∩ above(b))
    public static String computeJoin(String a, String b,
                                      Map<String, Set<String>> above,
                                      Map<String, Set<String>> below) {
        // common upper bounds
        Set<String> commonUpper = new HashSet<>(above.get(a));
        commonUpper.retainAll(above.get(b));
        if (commonUpper.isEmpty()) return null;

        // find the least among them - it must be below all others in the set
        for (String candidate : commonUpper) {
            boolean isLeast = commonUpper.stream()
                    .allMatch(other -> below.get(candidate).contains(other));
            if (isLeast) return candidate;
        }
        return null;
    }

    // Build the full meet table for all pairs
    public static Map<String, Map<String, String>> buildMeetTable(List<String> elements,
                                                                    Map<String, Set<String>> below,
                                                                    Map<String, Set<String>> above) {
        Map<String, Map<String, String>> table = new LinkedHashMap<>();
        for (String a : elements) {
            table.put(a, new LinkedHashMap<>());
            for (String b : elements) {
                String meet = computeMeet(a, b, below, above);
                table.get(a).put(b, meet != null ? meet : "—");
            }
        }
        return table;
    }

    // Build the full join table for all pairs
    public static Map<String, Map<String, String>> buildJoinTable(List<String> elements,
                                                                    Map<String, Set<String>> above,
                                                                    Map<String, Set<String>> below) {
        Map<String, Map<String, String>> table = new LinkedHashMap<>();
        for (String a : elements) {
            table.put(a, new LinkedHashMap<>());
            for (String b : elements) {
                String join = computeJoin(a, b, above, below);
                table.get(a).put(b, join != null ? join : "—");
            }
        }
        return table;
    }

    // -------------------------------------------------------------------------
    // 6. LATTICE DETECTION
    // -------------------------------------------------------------------------

    // A poset is a lattice only if every pair has both a meet AND a join
    public static boolean isLattice(List<String> elements,
                                     Map<String, Set<String>> above,
                                     Map<String, Set<String>> below) {
        for (String a : elements) {
            for (String b : elements) {
                if (computeMeet(a, b, below, above) == null) return false;
                if (computeJoin(a, b, above, below) == null) return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    // Convert relation set to "a,b" string set for fast lookup
    public static Set<String> toStringSet(Set<String[]> relations) {
        return relations.stream()
                .map(r -> r[0] + "," + r[1])
                .collect(Collectors.toSet());
    }

    // Classify the order type for display
    public static String classifyOrder(boolean reflexive, boolean symmetric,
                                        boolean antisymmetric, boolean transitive) {
        if (reflexive && antisymmetric && transitive)
            return "Partial Order (Reflexive, Antisymmetric, Transitive) — Hasse diagram can be drawn.";
        if (reflexive && symmetric && transitive)
            return "Equivalence Relation (Reflexive, Symmetric, Transitive).";
        if (!reflexive && antisymmetric && transitive)
            return "Strict Partial Order (Irreflexive, Antisymmetric, Transitive).";
        return "Not a standard partial order — Hasse diagram may not be meaningful.";
    }
}
