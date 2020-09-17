package io.github.honeyroasted.cereal.serialization;

import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.DelegateNode;

import java.util.HashMap;
import java.util.Map;

public class NodeCache {
    private Map<Integer, CerealNode> nodes = new HashMap<>();

    public CerealNode get(Object key) {
        return get(System.identityHashCode(key));
    }

    public CerealNode get(int key) {
        return nodes.computeIfAbsent(key, k -> CerealNode.delegate());
    }

    public void put(Object key, CerealNode node) {
        put(System.identityHashCode(key), node);
    }

    public void put(int key, CerealNode node) {
        CerealNode current = nodes.get(key);
        if (current instanceof DelegateNode) {
            ((DelegateNode) current).setNode(node);
        } else {
            nodes.put(key, node);
        }
    }

    public boolean containsKey(int id) {
        return this.nodes.containsKey(id);
    }

    public boolean containsKey(Object obj) {
        return containsKey(System.identityHashCode(obj));
    }
}
