package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.tree.CerealNode;

public abstract class SimpleSerializer<T> implements Cerealizer<T> {
    private Class<T> type;

    public SimpleSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public boolean accepts(Object obj) {
        return this.type.isInstance(obj);
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.isAssignableTo(JavaTypes.of(this.type));
    }

}
