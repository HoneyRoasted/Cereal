package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.serialization.SerializationException;
import io.github.honeyroasted.cereal.tree.CerealNode;

import java.util.Arrays;
import java.util.List;

public class ClassSerializer extends SimpleSerializer<Class> {
    private static final List<Class> CLASSES = Arrays.asList(
            boolean.class, byte.class, char.class, short.class, int.class, long.class, float.class, double.class, void.class,
            Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class,
            String.class
    );

    private List<Class> shortened;

    public ClassSerializer() {
        this(CLASSES);
    }

    public ClassSerializer(List<Class> shortened) {
        super(Class.class);
        this.shortened = shortened == null ? CLASSES : shortened;
    }

    @Override
    public Class deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        try {
            if (node.tryGetAs(Number.class).isPresent()) {
                return shortened.get(Byte.toUnsignedInt(node.getAsByte()));
            } else {
                return Class.forName(node.getAsString());
            }
        } catch (ClassNotFoundException e) {
            throw new SerializationException("Class not found", e);
        }
    }

    @Override
    public CerealNode serialize(Class obj, CerealizeRegistry registry, NodeCache cache) {
        int ind = shortened.indexOf(obj);
        if (ind != -1) {
            return CerealNode.of((byte) ind);
        } else {
            return CerealNode.of(obj.getName());
        }
    }

}
