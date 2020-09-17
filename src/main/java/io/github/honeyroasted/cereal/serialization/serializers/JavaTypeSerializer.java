package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.ArrayType;
import honeyroasted.javatype.GenericType;
import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import honeyroasted.javatype.VariableType;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.serialization.SerializationException;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;
import io.github.honeyroasted.cereal.tree.MapNode;

public class JavaTypeSerializer implements Cerealizer<JavaType> {

    @Override
    public boolean accepts(Object obj) {
        return obj instanceof JavaType;
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.isAssignableTo(JavaTypes.of(JavaType.class));
    }

    @Override
    public JavaType deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        String type = node.mapView().get("type").get().getAsString();
        if (type.equals("array")) {
            return deserialize(JavaTypes.of(JavaType.class), node.mapView().get("component").get(), registry, cache)
                    .array(node.mapView().get("dimension").get().getAsInt());
        } else if (type.equals("var")) {
            VariableType.Builder builder = VariableType.builder(node.mapView().get("name").get().getAsString());

            for (CerealNode upper : node.mapView().get("upper").get()) {
                builder.upper(deserialize(JavaTypes.of(JavaType.class), upper, registry, cache));
            }

            for (CerealNode lower : node.mapView().get("lower").get()) {
                builder.upper(deserialize(JavaTypes.of(JavaType.class), lower, registry, cache));
            }

            return builder.build();
        } else if (type.equals("generic")) {
            try {
                GenericType.Builder builder = GenericType.builder(Class.forName(node.mapView().get("class").get().getAsString()));

                for (CerealNode generic : node.mapView().get("generics").get()) {
                    builder.generic(deserialize(JavaTypes.of(JavaType.class), generic, registry, cache));
                }

                return builder.build();
            } catch (ClassNotFoundException e) {
                throw new SerializationException("No class for given name", e);
            }
        } else {
            return JavaTypes.OBJECT;
        }
    }

    @Override
    public CerealNode serialize(JavaType obj, CerealizeRegistry registry, NodeCache cache) {
        MapNode node = MapNode.create(JavaType.class);
        if (obj instanceof ArrayType) {
            node.put("type", "array");
            node.put("dimension", ((ArrayType) obj).getDimensions());
            node.put("component", serialize(((ArrayType) obj).getAbsoluteComponent(), registry, cache));
        } else if (obj instanceof VariableType) {
            node.put("type", "var");
            node.put("name", obj.getName());

            ListNode upper = ListNode.create();
            for (JavaType type : ((VariableType) obj).getUpper()) {
                upper.add(serialize(type, registry, cache));
            }

            ListNode lower = ListNode.create();
            for (JavaType type : ((VariableType) obj).getLower()) {
                lower.add(serialize(type, registry, cache));
            }

            node.put("upper", upper);
            node.put("lower", lower);
        } else if (obj instanceof GenericType) {
            node.put("type", "generic");
            node.put("class", obj.getType().getName());

            ListNode generics = ListNode.create();
            for (JavaType gen : ((GenericType) obj).getGenerics()) {
                generics.add(serialize(gen, registry, cache));
            }

            node.put("generics", generics);
        }
        return node;
    }

}
