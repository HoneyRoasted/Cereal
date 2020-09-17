package io.github.honeyroasted.cereal.tree;

public enum NodeType {

    PRIMITIVE,
    MAP,
    LIST;

    boolean canHaveChildren() {
        return this == MAP || this == LIST;
    }

}
