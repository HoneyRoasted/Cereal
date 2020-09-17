package io.github.honeyroasted.cereal.tree;

import honeyroasted.javatype.JavaTypes;
import honeyroasted.javatype.Token;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface CerealNode extends Iterable<CerealNode> {

    static CerealNode of(Object obj) {
        if (obj instanceof CerealNode) {
            return (CerealNode) obj;
        } else {
            return new PrimitiveNode(obj);
        }
    }

    static Set<Integer> usedIds(CerealNode node) {
        Set<Integer> ids = new HashSet<>();
        collectUsedIds(node, ids);
        return ids;
    }

    static void collectUsedIds(CerealNode node, Set<Integer> ids) {
        if (node.type() != NodeType.PRIMITIVE) {
            if (node.type() == NodeType.MAP) {
                MapNode mapNode = node.mapView();
                if (mapNode.getMeta("type").map(c -> c.getAsString().equals("ref")).orElse(false)) {
                    ids.add(mapNode.getMeta("id").get().getAsInt());
                }
            }

            for (CerealNode child : node) {
                collectUsedIds(child, ids);
            }
        }
    }

    static String flatToString(CerealNode node) {
        return toString(node, -1);
    }

    static String toString(CerealNode node) {
        return toString(node, 0);
    }

    static String toString(CerealNode node, int indent) {
        String nl = indent < 0 ? "" : "\n";
        int nxInd = indent < 0 ? -1 : indent + 1;

        String mind1 = "", mind2 = "";

        StringBuilder ind = new StringBuilder();
        for (int i = 0; i < indent + 1; i++) {
            ind.append(" ");
            if (i == indent) {
                mind1 = ind.toString();
            }
        }
        mind2 = ind.toString();

        String ind1 = mind1, ind2 = mind2;

        if (node.type() == NodeType.PRIMITIVE) {
            return node.isNull() ? "null" : String.valueOf(node.get());
        } else if (node.type() == NodeType.LIST) {
            ListNode list = node.listView();
            StringBuilder builder = new StringBuilder();
            builder.append("[").append(nl);
            for (int i = 0; i < list.size(); i++) {
                int finalI = i;
                list.get(i).ifPresent(n -> {
                    builder.append(ind2).append(toString(n, nxInd));
                    if (finalI != list.size() - 1) {
                        builder.append(", ").append(nl);
                    }
                });
            }
            builder.append(nl).append(ind1).append("]");
            return builder.toString();
        } else if (node.type() == NodeType.MAP) {
            MapNode map = node.mapView();
            StringBuilder builder = new StringBuilder();
            builder.append("{").append(nl);
            final int[] i = {0};
            map.keys().forEach(k -> {
                map.get(k).ifPresent(c -> {
                    builder.append(ind2).append(k).append("=").append(toString(c, nxInd));
                    if (i[0] != map.size() - 1) {
                        builder.append(", ").append(nl);
                    }
                    i[0]++;
                });
            });
            builder.append(nl).append(ind1).append("}");
            return builder.toString();
        } else {
            return "?";
        }
    }

    static DelegateNode delegate(CerealNode node) {
        return new DelegateNode(node);
    }

    static DelegateNode delegate() {
        return delegate(null);
    }

    Optional<Class<?>> represented();

    NodeType type();

    MapNode mapView();

    ListNode listView();

    CerealNode deRecursify(NodeCache cache);

    CerealNode reRecrusify(NodeCache cache);

    CerealNode removeUnusedIds(Set<Integer> ids);

    default CerealNode removeUnusedIds() {
        return removeUnusedIds(CerealNode.usedIds(this));
    }

    default <T> Optional<T> tryGetAs(Class<T> type) {
        if (type.isInstance(this)) {
            return Optional.of((T) this);
        } else {
            return Optional.empty();
        }
    }

    default <T> T getAs(Class<T> type) {
        if (type.isInstance(this)) {
            return (T) this;
        } else {
            throw new NoSuchElementException("Type requested: " + type.getName() + ", value type: " + this.getClass().getName());
        }
    }

    default <T> T deserialize(Token<? extends T>  typeToken, CerealizeRegistry registry) {
        return Cereal.deSerializeTree(typeToken.resolve(), this, registry);
    }

    default <T> T deserialize(Token<? extends T> typeToken) {
        return deserialize(typeToken, CerealizeRegistry.GLOBAL);
    }

    default <T> T deserialize(Class<? extends T> type) {
        return Cereal.deSerializeTree(JavaTypes.of(type), this, CerealizeRegistry.GLOBAL);
    }

    default boolean isNull() {
        return false;
    }

    default Object get() {
        return isNull() ? null : getAs(Object.class);
    }

    default MapNode getAsObject() {
        return getAs(MapNode.class);
    }

    default ListNode getAsList() {
        return getAs(ListNode.class);
    }

    default String getAsString() {
        return getAs(String.class);
    }

    default Enum<?> getAsEnum() {
        return getAs(Enum.class);
    }

    default Class<?> getAsClass() {
        return getAs(Class.class);
    }

    default Number getAsNumber() {
        return getAs(Number.class);
    }

    default Boolean getAsBoolean() {
        return getAs(Boolean.class);
    }

    default Character getAsChar() {
        return getAs(Character.class);
    }

    default byte getAsByte() {
        return getAsNumber().byteValue();
    }

    default short getAsShort() {
        return getAsNumber().shortValue();
    }

    default int getAsInt() {
        return getAsNumber().intValue();
    }

    default long getAsLong() {
        return getAsNumber().longValue();
    }

    default float getAsFloat() {
        return getAsNumber().floatValue();
    }

    default double getAsDouble() {
        return getAsNumber().doubleValue();
    }

    default Stream<CerealNode> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    int size();

    default Optional<CerealNode> query(NodePath path) {
        return path.query(this);
    }

    default Optional<CerealNode> query(Object... path) {
        return NodePath.of(path).query(this);
    }

}
