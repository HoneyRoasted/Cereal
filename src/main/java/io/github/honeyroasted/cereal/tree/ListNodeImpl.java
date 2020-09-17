package io.github.honeyroasted.cereal.tree;

import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListNodeImpl implements ListNode {
    private List<CerealNode> nodes = new ArrayList<>();
    private Class<?> represented;

    public ListNodeImpl(Class<?> represented) {
        this.represented = represented;
    }

    @Override
    public Optional<CerealNode> get(int index) {
        return index < 0 || index >= nodes.size() ? Optional.empty() : Optional.ofNullable(this.nodes.get(index));
    }

    @Override
    public boolean add(int index, CerealNode obj) {
        if (index < 0 || index > nodes.size()) {
            return false;
        } else {
            nodes.add(index, obj);
            return true;
        }
    }

    @Override
    public boolean set(int index, CerealNode obj) {
        if (index < 0 || index >= nodes.size()) {
            return false;
        } else {
            nodes.set(index, obj);
            return true;
        }
    }

    @Override
    public Optional<Class<?>> represented() {
        return Optional.ofNullable(this.represented);
    }

    @Override
    public NodeType type() {
        return NodeType.LIST;
    }

    @Override
    public MapNode mapView() {
        ListNode self = this;
        return new MapNode() {
            @Override
            public Iterator<CerealNode> iterator() {
                return self.iterator();
            }

            @Override
            public Optional<CerealNode> get(String key) {
                try {
                    int query = Integer.parseInt(key);
                    return self.get(query);
                } catch (NumberFormatException e) {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<CerealNode> getMeta(String key) {
                return Optional.empty();
            }

            @Override
            public boolean putExactly(String key, CerealNode node) {
                try {
                    int query = Integer.parseInt(key);
                    return self.set(query, node);
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public boolean put(String key, CerealNode node) {
                try {
                    int query = Integer.parseInt(key);
                    return self.set(query, node);
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public boolean putMeta(String key, CerealNode node) {
                return false;
            }

            @Override
            public Set<String> keys() {
                return IntStream.range(0, size()).mapToObj(String::valueOf).collect(Collectors.toSet());
            }

            @Override
            public Optional<Class<?>> represented() {
                return Optional.ofNullable(represented);
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
                return self;
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
    public ListNode listView() {
        return this;
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
            ListNodeImpl node = new ListNodeImpl(this.represented);
            cache.put(id, node);

            MapNode header = MapNode.create();
            header.putMeta("id", CerealNode.of(id));
            header.putMeta("type", CerealNode.of("header"));

            node.nodes.add(header);
            this.nodes.forEach(n -> node.nodes.add(n.deRecursify(cache)));
            return node;
        }
    }

    @Override
    public CerealNode reRecrusify(NodeCache cache) {
        ListNodeImpl node = new ListNodeImpl(this.represented);
        int i = 0;

        if (this.get(0).isPresent()) {
            CerealNode head = this.get(0).get();
            if (head.type() == NodeType.MAP && head.mapView().getMeta("type").map(s -> s.getAsString().equals("header")).orElse(false)) {
                cache.put(head.mapView().getMeta("id").get().getAsInt(), node);
                i = 1;
            }
        }

        for (int j = i; j < this.nodes.size(); j++) {
            node.add(this.nodes.get(j).reRecrusify(cache));
        }

        return node;
    }

    @Override
    public CerealNode removeUnusedIds(Set<Integer> ids) {
        ListNodeImpl node = new ListNodeImpl(this.represented);
        int i = 0;
        if (this.get(0).isPresent()) {
            CerealNode head = this.get(0).get();
            if (head.type() == NodeType.MAP && head.mapView().getMeta("type").map(s -> s.getAsString().equals("header")).orElse(false) &&
                    !head.mapView().getMeta("id").map(c -> ids.contains(c.getAsInt())).orElse(true)) {
                i++;
            }
        }

        for (int j = i; j < this.size(); j++) {
            node.add(this.nodes.get(j).removeUnusedIds(ids));
        }

        return node;
    }

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    public Iterator<CerealNode> iterator() {
        return this.nodes.iterator();
    }

}
