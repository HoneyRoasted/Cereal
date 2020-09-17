package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.serialization.SerializationException;
import io.github.honeyroasted.cereal.tree.CerealNode;

import java.util.stream.Stream;

public class EnumSerializer implements Cerealizer<Enum<?>> {

    @Override
    public boolean accepts(Object obj) {
        return obj instanceof Enum;
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return Enum.class.isAssignableFrom(target.getType());
    }

    @Override
    public Enum<?> deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        return (Enum<?>) Stream.of(target.getType().getEnumConstants()).filter(e -> ((Enum) e).name().equals(node.getAsString())).findFirst().orElseThrow(() ->
            new SerializationException("No enum constant for name: " + node.getAsString()));
    }

    @Override
    public CerealNode serialize(Enum<?> obj, CerealizeRegistry registry, NodeCache cache) {
        return CerealNode.of(obj.name());
    }

}
