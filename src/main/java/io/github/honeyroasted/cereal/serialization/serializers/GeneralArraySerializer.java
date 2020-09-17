package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;

import java.lang.reflect.Array;

public class GeneralArraySerializer implements Cerealizer<Object> {

    @Override
    public boolean accepts(Object obj) {
        return obj != null && obj.getClass().isArray() && JavaTypes.ofParameterized(getFinalComponent(obj.getClass())).genericCount() == 0;
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.isArray() && JavaTypes.ofParameterized(getFinalComponent(target.getType())).genericCount() == 0;
    }

    private Class<?> getFinalComponent(Class<?> cls) {
        return cls.isArray() ? getFinalComponent(cls.getComponentType()) : cls;
    }

    @Override
    public Object deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        Object array = Array.newInstance(target.getType().getComponentType(), node.size());
        cache.put(node, array);
        for (int i = 0; i < node.size(); i++) {
            CerealNode e = node.listView().get(i).get();
            Array.set(array, i, Cereal.deserializeIsolated(e, registry, cache));
        }
        return array;
    }

    @Override
    public CerealNode serialize(Object obj, CerealizeRegistry registry, NodeCache cache) {
        ListNode node = ListNode.create(obj.getClass());
        cache.put(obj, node);
        int size = Array.getLength(obj);
        for (int i = 0; i < size; i++) {
            Object object = Array.get(obj, i);
            node.add(Cereal.serializeIsolated(object, registry, cache));
        }
        return node;
    }

}
