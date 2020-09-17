package io.github.honeyroasted.cereal.tree;

import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class PrimitiveNode implements CerealNode {
    private Object val;
    private Class<?> represented;

    public PrimitiveNode(Object val) {
        this.val = val;
        this.represented = val == null ? null : val.getClass();

        if (val != null && !(val instanceof String) && !JavaTypes.unbox(val.getClass()).isPrimitive()) {
            throw new IllegalArgumentException("Illegal primitive: " + val.getClass());
        }
    }

    @Override
    public boolean isNull() {
        return this.val == null;
    }

    @Override
    public <T> T getAs(Class<T> type) {
        if (type.isInstance(val)) {
            return (T) val;
        } else if (type.isInstance(this)) {
            return (T) this;
        } else {
            throw new NoSuchElementException("Type requested: " + type.getName() + ", value type: " + (val == null ? "null" : val.getClass().getName()));
        }
    }

    @Override
    public <T> Optional<T> tryGetAs(Class<T> type) {
        if (type.isInstance(val)) {
            return Optional.of((T) val);
        } else if (type.isInstance(this)) {
            return Optional.of((T) this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Class<?>> represented() {
        return Optional.ofNullable(represented);
    }

    @Override
    public NodeType type() {
        return NodeType.PRIMITIVE;
    }

    @Override
    public MapNode mapView() {
        PrimitiveNode self = this;
        return new MapNode() {
            @Override
            public Iterator<CerealNode> iterator() {
                return self.iterator();
            }

            @Override
            public Optional<CerealNode> get(String key) {
                return Optional.of(self);
            }

            @Override
            public Optional<CerealNode> getMeta(String key) {
                return Optional.empty();
            }

            @Override
            public boolean putExactly(String key, CerealNode node) {
                if (node instanceof PrimitiveNode) {
                    self.val = node.get();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean put(String key, CerealNode node) {
                if (node instanceof PrimitiveNode) {
                    self.val = node.get();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean putMeta(String key, CerealNode node) {
                return false;
            }

            @Override
            public Set<String> keys() {
                Set<String> keys = new HashSet<>();
                keys.add("");
                return keys;
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
                return self.listView();
            }

            @Override
            public CerealNode deRecursify(NodeCache cache) {
                return self;
            }

            @Override
            public CerealNode reRecrusify(NodeCache cache) {
                return self;
            }

            @Override
            public CerealNode removeUnusedIds(Set<Integer> ids) {
                return self;
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public String toString() {
                return self.toString();
            }
        };
    }

    @Override
    public ListNode listView() {
        PrimitiveNode self = this;
        return new ListNode() {
            @Override
            public Iterator<CerealNode> iterator() {
                return self.iterator();
            }

            @Override
            public Optional<CerealNode> get(int index) {
                return Optional.of(self);
            }

            @Override
            public boolean add(int index, CerealNode node) {
                if (node instanceof PrimitiveNode) {
                    self.val = node.get();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean set(int index, CerealNode node) {
                if (node instanceof PrimitiveNode) {
                    self.val = node.get();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Optional<Class<?>> represented() {
                return Optional.ofNullable(represented);
            }

            @Override
            public NodeType type() {
                return NodeType.LIST;
            }

            @Override
            public MapNode mapView() {
                return self.mapView();
            }

            @Override
            public ListNode listView() {
                return this;
            }

            @Override
            public CerealNode deRecursify(NodeCache cache) {
                return self;
            }

            @Override
            public CerealNode reRecrusify(NodeCache cache) {
                return self;
            }

            @Override
            public CerealNode removeUnusedIds(Set<Integer> ids) {
                return self;
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public String toString() {
                return self.toString();
            }
        };
    }

    @Override
    public CerealNode deRecursify(NodeCache cache) {
        return this;
    }

    @Override
    public CerealNode reRecrusify(NodeCache cache) {
        return this;
    }

    @Override
    public CerealNode removeUnusedIds(Set<Integer> ids) {
        return this;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Iterator<CerealNode> iterator() {
        return Arrays.<CerealNode>asList(this).iterator();
    }

    @Override
    public String toString() {
        return this.isNull() ? "null" : String.valueOf(this.val);
    }
}
