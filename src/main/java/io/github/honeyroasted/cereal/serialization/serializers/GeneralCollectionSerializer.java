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
import java.util.Collection;

public class GeneralCollectionSerializer implements Cerealizer<Collection> {

    @Override
    public boolean accepts(Object obj) {
        return obj instanceof Collection && acceptsCls(obj.getClass());
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.isAssignableTo(JavaTypes.of(Collection.class));
    }

    private boolean acceptsCls(Class<?> cls) {
        try {
            return Modifier.isPublic(cls.getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public Collection deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        try {
            Collection c = (Collection) Cereal.<Class>deserialize(JavaTypes.of(Class.class), node.listView().get(0).get(), registry, cache).getConstructor().newInstance();
            cache.put(node, c);
            for (CerealNode sub : node.listView().get(1).get()) {
                c.add(Cereal.deserializeIsolated(sub, registry, cache));
            }
            return c;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SerializationException("Failed to create new collection instance", e);
        }
    }

    @Override
    public CerealNode serialize(Collection obj, CerealizeRegistry registry, NodeCache cache) {
        ListNode node = ListNode.create(obj.getClass());
        cache.put(obj, node);
        node.add(Cereal.serialize(obj.getClass(), registry, cache));

        ListNode elements = ListNode.create();
        node.add(elements);

        for (Object o : obj) {
            elements.add(Cereal.serializeIsolated(o, registry, cache));
        }

        return node;
    }

}
