package io.github.honeyroasted.cereal.serialization;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;

public class Cereal {

    public static CerealNode serializeTree(Object obj, CerealizeRegistry registry) {
        return serialize(obj, registry, new NodeCache()).deRecursify(new NodeCache()).removeUnusedIds();
    }

    public static <T> T deSerializeTree(JavaType cls, CerealNode node, CerealizeRegistry registry) {
        ObjCache cache = new ObjCache();
        T t = deserialize(cls, node.reRecrusify(new NodeCache()), registry, cache);
        cache.inject();
        return t;
    }

    public static CerealNode serializeIsolated(Object obj, CerealizeRegistry registry, NodeCache cache) {
        if (obj == null) {
            return CerealNode.of(null);
        }

        ListNode node = ListNode.create(obj.getClass());
        node.add(Cereal.serialize(obj.getClass(), registry, cache));
        node.add(serialize(obj, registry, cache));
        return node;
    }

    public static <T> T deserializeIsolated(CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        if (node.isNull()) {
            return null;
        }

        Class<?> cls = Cereal.deserialize(JavaTypes.of(Class.class), node.listView().get(0).get(), registry, cache);
        return deserialize(JavaTypes.of(cls), node.listView().get(1).get(), registry, cache);
    }

    public static CerealNode serialize(Object obj, CerealizeRegistry registry, NodeCache cache) {
        if (cache.containsKey(obj)) {
            return cache.get(obj);
        }
        return registry.getFor(obj).orElseThrow(() -> new IllegalArgumentException("No serializer for: " + obj)).serialize(obj, registry, cache);
    }

    public static <T> T deserialize(JavaType cls, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        if (cache.containsKey(node)) {
            return (T) cache.get(node);
        }
        return (T) registry.getFor(cls, node).orElseThrow(() -> new IllegalArgumentException("No serializer for: " + cls)).deserialize(cls, node, registry, cache);
    }

}
