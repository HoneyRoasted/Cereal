package io.github.honeyroasted.cereal.tree;

import java.util.Optional;

public interface ListNode extends CerealNode {

    static ListNode create(Class<?> represented) {
        return new ListNodeImpl(represented);
    }

    static ListNode create() {
        return new ListNodeImpl(null);
    }

    Optional<CerealNode> get(int index);

    default boolean add(Object obj) {
        return add(CerealNode.of(obj));
    }

    default boolean add(int index, Object obj) {
        return add(index, CerealNode.of(obj));
    }

    default boolean set(int index, Object obj) {
        return set(index, CerealNode.of(obj));
    }

    default boolean add(CerealNode obj) {
       return add(size(), obj);
    }

    boolean add(int index, CerealNode obj);

    boolean set(int index, CerealNode obj);

}
