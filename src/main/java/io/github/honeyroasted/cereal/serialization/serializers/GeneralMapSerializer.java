package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.serialization.SerializationException;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;

public class GeneralMapSerializer implements Cerealizer<Map> {

    @Override
    public boolean accepts(Object obj) {
        return obj instanceof Map && acceptsCls(obj.getClass());
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.isAssignableTo(JavaTypes.of(Map.class));
    }

    private boolean acceptsCls(Class<?> cls) {
        try {
            return Modifier.isPublic(cls.getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public Map deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        try {
            Map c = (Map) Cereal.<Class>deserialize(JavaTypes.of(Class.class), node.listView().get(0).get(), registry, cache).getConstructor().newInstance();
            cache.put(node, c);

            for (CerealNode element : node.listView().get(1).get()) {
                c.put(Cereal.deserializeIsolated(element.listView().get(0).get(), registry, cache),
                        Cereal.deserializeIsolated(element.listView().get(1).get(), registry, cache));
            }

            return c;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SerializationException("Failed to create new map instance", e);
        }
    }

    @Override
    public CerealNode serialize(Map obj, CerealizeRegistry registry, NodeCache cache) {
        ListNode node = ListNode.create(obj.getClass());
        cache.put(obj, node);
        node.add(Cereal.serialize(obj.getClass(), registry, cache));

        ListNode elements = ListNode.create();
        node.add(elements);

        obj.forEach((key, value) -> {
            ListNode entry = ListNode.create();
            elements.add(entry);

            entry.add(Cereal.serializeIsolated(key, registry, cache));
            entry.add(Cereal.serializeIsolated(value, registry, cache));
        });

        return node;
    }

}
