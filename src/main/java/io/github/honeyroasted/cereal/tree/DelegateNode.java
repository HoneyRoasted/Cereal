package io.github.honeyroasted.cereal.tree;

import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class DelegateNode implements CerealNode {
    private CerealNode node;

    public DelegateNode(CerealNode node) {
        this.node = node;
    }

    public CerealNode getNode() {
        return node;
    }

    public void setNode(CerealNode node) {
        this.node = node;
    }

    @Override
    public Optional<Class<?>> represented() {
        checkPresent();
        return this.node.represented();
    }

    @Override
    public NodeType type() {
        checkPresent();
        return this.node.type();
    }

    @Override
    public MapNode mapView() {
        checkPresent();
        return this.node.mapView();
    }

    @Override
    public ListNode listView() {
        checkPresent();
        return this.node.listView();
    }

    @Override
    public CerealNode deRecursify(NodeCache cache) {
        return this.node == null ? this : this.node.deRecursify(cache);
    }

    @Override
    public CerealNode reRecrusify(NodeCache cache) {
        return this.node == null ? this : this.node.reRecrusify(cache);
    }

    @Override
    public CerealNode removeUnusedIds(Set<Integer> ids) {
        return this.node == null ? this : this.node.removeUnusedIds(ids);
    }

    @Override
    public int size() {
        checkPresent();
        return this.node.size();
    }

    @Override
    public <T> T getAs(Class<T> type) {
        checkPresent();
        return this.node.getAs(type);
    }

    @Override
    public <T> Optional<T> tryGetAs(Class<T> type) {
        checkPresent();
        return this.node.tryGetAs(type);
    }

    private void checkPresent() {
        if (this.node == null) {
            throw new NoSuchElementException("Delegate node unfilled");
        }
    }

    @Override
    public Iterator<CerealNode> iterator() {
        return this.node.iterator();
    }

    @Override
    public String toString() {
        return this.node == null ? "<absent>" : this.node.toString();
    }

}
