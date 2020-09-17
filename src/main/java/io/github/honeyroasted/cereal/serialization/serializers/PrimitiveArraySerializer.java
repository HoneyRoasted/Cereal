package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.ArrayType;
import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;
import io.github.honeyroasted.cereal.serialization.NodeCache;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

public class PrimitiveArraySerializer implements Cerealizer<Object> {

    @Override
    public boolean accepts(Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            Class<?> component = getFinalComponent(obj.getClass());
            return component.isPrimitive() || (Modifier.isFinal(component.getModifiers()) &&
                    JavaTypes.ofParameterized(component).genericCount() == 0);
        }

        return false;
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        if (target.isArray()) {
            JavaType component = ((ArrayType) target).getAbsoluteComponent();
            return component.isPrimitive() || (Modifier.isFinal(component.getType().getModifiers()) &&
                    JavaTypes.ofParameterized(component.getType()).genericCount() == 0);
        }
        return false;
    }

    private Class<?> getFinalComponent(Class<?> cls) {
        return cls.isArray() ? getFinalComponent(cls.getComponentType()) : cls;
    }

    @Override
    public Object deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        Class<?> comp = target.getType().getComponentType();
        Object arr = Array.newInstance(comp, node.size());
        cache.put(node, arr);
        for (int i = 0; i < node.size(); i++) {
            Array.set(arr, i, Cereal.deserialize(target.array(((ArrayType) target).getDimensions() - 1), node.listView().get(i).get(), registry, cache));
        }
        return arr;
    }

    @Override
    public CerealNode serialize(Object obj, CerealizeRegistry registry, NodeCache cache) {
        ListNode node = ListNode.create(obj.getClass());
        cache.put(obj, node);
        for (int i = 0; i < Array.getLength(obj); i++) {
            node.add(Cereal.serialize(Array.get(obj, i), registry, cache));
        }
        return node;
    }
}
