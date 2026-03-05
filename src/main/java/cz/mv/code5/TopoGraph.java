package cz.mv.code5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TopoGraph {
    private final Map<Integer, Set<Integer>> adjacency = new HashMap<>();

    public void addRule(int from, int to) {
        adjacency
                .computeIfAbsent(from, k -> new HashSet<>())
                .add(to);

        adjacency.computeIfAbsent(to, k -> new HashSet<>());
    }

    public boolean isCorrectOrder(int previous, int current) {
        Set<Integer> targets = adjacency.get(current);

        if (targets == null) {
            return true;
        }

        return !targets.contains(previous);
    }
}
