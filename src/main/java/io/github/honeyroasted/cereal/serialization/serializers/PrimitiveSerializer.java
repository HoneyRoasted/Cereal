package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.tree.NodeType;

public class PrimitiveSerializer implements Cerealizer<Object> {

    @Override
    public boolean accepts(Object obj) {
        return obj instanceof Number || obj instanceof Boolean || obj instanceof Character || obj instanceof String || obj == null;
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return node.isNull() || (node.type() == NodeType.PRIMITIVE && (target.unbox().isPrimitive() || target.isAssignableTo(JavaTypes.of(String.class))));
    }

    @Override
    public Object deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        return node.get();
    }

    @Override
    public CerealNode serialize(Object obj, CerealizeRegistry registry, NodeCache cache) {
        return CerealNode.of(obj);
    }

}
