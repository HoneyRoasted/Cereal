package io.github.honeyroasted.cereal.tree;

import java.util.Optional;
import java.util.Set;

public interface MapNode extends CerealNode {

    static MapNode create() {
        return new MapNodeImpl(null);
    }

    static MapNode create(Class<?> represented) {
        return new MapNodeImpl(represented);
    }

    Optional<CerealNode> get(String key);

    Optional<CerealNode> getMeta(String key);

    boolean putExactly(String key, CerealNode node);

    default boolean putExactly(String key, Object obj) {
        return putExactly(key, CerealNode.of(obj));
    }

    boolean put(String key, CerealNode node);

    default boolean put(String key, Object obj) {
       return put(key, CerealNode.of(obj));
    }

    boolean putMeta(String key, CerealNode node);

    default boolean putMeta(String key, Object obj) {
        return putMeta(key, CerealNode.of(obj));
    }

    Set<String> keys();

}
