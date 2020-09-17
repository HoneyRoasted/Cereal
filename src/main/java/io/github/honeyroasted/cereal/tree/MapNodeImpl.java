package io.github.honeyroasted.cereal.tree;

import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MapNodeImpl implements MapNode {
    Map<String, CerealNode> nodes = new LinkedHashMap<>();
    private Class<?> represented;

    public MapNodeImpl(Class<?> represented) {
        this.represented = represented;
    }

    @Override
    public Optional<CerealNode> get(String key) {
        return Optional.ofNullable(nodes.get(key));
    }

    @Override
    public Optional<CerealNode> getMeta(String key) {
        return Optional.ofNullable(this.nodes.get("." + key));
    }

    @Override
    public boolean putExactly(String key, CerealNode node) {
        nodes.put(key, node);
        return true;
    }

    @Override
    public boolean put(String key, CerealNode node) {
        if (key.startsWith(".")) {
            throw new IllegalArgumentException("Key may not start with: .");
        }

        nodes.put(key, node);
        return true;
    }

    @Override
    public boolean putMeta(String key, CerealNode node) {
        nodes.put("." + key, node);
        return true;
    }

    @Override
    public Set<String> keys() {
        return new LinkedHashSet<>(this.nodes.keySet());
    }

    @Override
    public Optional<Class<?>> represented() {
        return Optional.of(represented);
    }

    @Override
    public NodeType type() {
        return NodeType.MAP;
    }

    @Override
    public MapNode mapView() {
        return this;
    }

    @Override
    public ListNode listView() {
        MapNode self = this;
        return new ListNode() {
            @Override
            public Iterator<CerealNode> iterator() {
                return self.iterator();
            }

            @Override
            public Optional<CerealNode> get(int index) {
                return self.get(String.valueOf(index));
            }

            @Override
            public boolean add(int index, CerealNode obj) {
                return self.put(String.valueOf(index), obj);
            }

            @Override
            public boolean set(int index, CerealNode obj) {
                return self.put(String.valueOf(index), obj);
            }

            @Override
            public Optional<Class<?>> represented() {
                return Optional.empty();
            }

            @Override
            public NodeType type() {
                return NodeType.LIST;
            }

            @Override
            public MapNode mapView() {
                return self;
            }

            @Override
            public ListNode listView() {
                return this;
            }

            @Override
            public CerealNode deRecursify(NodeCache cache) {
                return self.deRecursify(cache);
            }

            @Override
            public CerealNode reRecrusify(NodeCache cache) {
                return self.reRecrusify(cache);
            }

            @Override
            public CerealNode removeUnusedIds(Set<Integer> ids) {
                return self.removeUnusedIds(ids);
            }

            @Override
            public int size() {
                return self.size();
            }

            @Override
            public String toString() {
                return self.toString();
            }
        };
    }

    @Override
    public CerealNode deRecursify(NodeCache cache) {
        int id = System.identityHashCode(this);
        if (cache.containsKey(id)) {
            MapNode node = MapNode.create(this.represented);
            node.putMeta("id", CerealNode.of(id));
            node.putMeta("type", CerealNode.of("ref"));
            return node;
        } else {
            MapNode node = MapNode.create(this.represented);
            cache.put(id, node);
            node.putMeta("id", CerealNode.of(id));
            this.nodes.forEach((key, val) -> {
                if (!key.equals(".id")) {
                    node.putExactly(key, val.deRecursify(cache));
                }
            });
            return node;
        }
    }

    @Override
    public CerealNode reRecrusify(NodeCache cache) {
        if (this.getMeta("type").isPresent() && this.getMeta("type").get().getAsString().equals("ref")) {
            return cache.get(this.getMeta("id").get().getAsInt());
        } else {
            MapNode node = MapNode.create(this.represented);
            this.nodes.forEach((key, val) -> {
                node.putExactly(key, val.reRecrusify(cache));
            });

            if (this.getMeta("id").isPresent()) {
                cache.put(this.getMeta("id").get().getAsInt(), node);
            }
            return node;
        }
    }

    @Override
    public CerealNode removeUnusedIds(Set<Integer> ids) {
        MapNode node = MapNode.create(this.represented);
        this.nodes.forEach((key, val) -> {
            if (!key.equals(".id") || ids.contains(val.getAsInt())) {
                node.putExactly(key, val.removeUnusedIds(ids));
            }
        });
        return node;
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public Iterator<CerealNode> iterator() {
        return this.nodes.values().iterator();
    }

}
