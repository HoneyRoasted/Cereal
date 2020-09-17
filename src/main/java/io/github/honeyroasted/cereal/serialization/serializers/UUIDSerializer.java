package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;

import java.util.UUID;

public class UUIDSerializer extends SimpleSerializer<UUID> {

    public UUIDSerializer() {
        super(UUID.class);
    }

    @Override
    public UUID deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        return new UUID(node.listView().get(0).get().getAsLong(), node.listView().get(1).get().getAsLong());
    }

    @Override
    public CerealNode serialize(UUID obj, CerealizeRegistry registry, NodeCache cache) {
        ListNode list = ListNode.create(UUID.class);
        list.add(obj.getMostSignificantBits());
        list.add(obj.getLeastSignificantBits());
        return list;
    }

}
