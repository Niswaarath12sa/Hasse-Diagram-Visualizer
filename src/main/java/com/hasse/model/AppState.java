package com.hasse.model;

import java.util.*;

/**
 * AppState - carries all data between screens.
 * Algorithm results are stored here after DMATEngine computes them.
 */
public class AppState {

    private List<String> elements = new ArrayList<>();
    private Set<String[]> relations = new LinkedHashSet<>();
    private Set<String[]> reducedRelations = new LinkedHashSet<>();
    private String relationType = "divides";

    // basic relation properties
    private boolean reflexive;
    private boolean symmetric;
    private boolean transitive;
    private boolean antisymmetric;

    // reduction steps for display
    private List<String> reductionSteps = new ArrayList<>();

    // poset analysis results
    private List<String> minimalElements = new ArrayList<>();
    private List<String> maximalElements = new ArrayList<>();
    private String leastElement = null;
    private String greatestElement = null;
    private boolean lattice = false;

    // meet and join tables
    private Map<String, Map<String, String>> meetTable = new LinkedHashMap<>();
    private Map<String, Map<String, String>> joinTable = new LinkedHashMap<>();

    // reachability maps (used for analysis)
    private Map<String, Set<String>> above = new HashMap<>();
    private Map<String, Set<String>> below = new HashMap<>();

    public List<String> getElements() { return elements; }
    public void setElements(List<String> elements) { this.elements = elements; }

    public Set<String[]> getRelations() { return relations; }
    public void setRelations(Set<String[]> relations) { this.relations = relations; }

    public Set<String[]> getReducedRelations() { return reducedRelations; }
    public void setReducedRelations(Set<String[]> r) { this.reducedRelations = r; }

    public String getRelationType() { return relationType; }
    public void setRelationType(String t) { this.relationType = t; }

    public boolean isReflexive() { return reflexive; }
    public void setReflexive(boolean v) { this.reflexive = v; }

    public boolean isSymmetric() { return symmetric; }
    public void setSymmetric(boolean v) { this.symmetric = v; }

    public boolean isTransitive() { return transitive; }
    public void setTransitive(boolean v) { this.transitive = v; }

    public boolean isAntisymmetric() { return antisymmetric; }
    public void setAntisymmetric(boolean v) { this.antisymmetric = v; }

    public List<String> getReductionSteps() { return reductionSteps; }
    public void setReductionSteps(List<String> steps) { this.reductionSteps = steps; }

    public List<String> getMinimalElements() { return minimalElements; }
    public void setMinimalElements(List<String> v) { this.minimalElements = v; }

    public List<String> getMaximalElements() { return maximalElements; }
    public void setMaximalElements(List<String> v) { this.maximalElements = v; }

    public String getLeastElement() { return leastElement; }
    public void setLeastElement(String v) { this.leastElement = v; }

    public String getGreatestElement() { return greatestElement; }
    public void setGreatestElement(String v) { this.greatestElement = v; }

    public boolean isLattice() { return lattice; }
    public void setLattice(boolean v) { this.lattice = v; }

    public Map<String, Map<String, String>> getMeetTable() { return meetTable; }
    public void setMeetTable(Map<String, Map<String, String>> t) { this.meetTable = t; }

    public Map<String, Map<String, String>> getJoinTable() { return joinTable; }
    public void setJoinTable(Map<String, Map<String, String>> t) { this.joinTable = t; }

    public Map<String, Set<String>> getAbove() { return above; }
    public void setAbove(Map<String, Set<String>> v) { this.above = v; }

    public Map<String, Set<String>> getBelow() { return below; }
    public void setBelow(Map<String, Set<String>> v) { this.below = v; }
}
