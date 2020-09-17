package io.github.honeyroasted.cereal.serialization;

import honeyroasted.javatype.JavaType;
import io.github.honeyroasted.cereal.serialization.serializers.AnnotatedSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.ClassSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.EnumSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.GeneralArraySerializer;
import io.github.honeyroasted.cereal.serialization.serializers.GeneralCollectionSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.GeneralMapSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.JavaTypeSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.PrimitiveArraySerializer;
import io.github.honeyroasted.cereal.serialization.serializers.PrimitiveSerializer;
import io.github.honeyroasted.cereal.serialization.serializers.UUIDSerializer;
import io.github.honeyroasted.cereal.tree.CerealNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CerealizeRegistry {
    public static final CerealizeRegistry GLOBAL = new CerealizeRegistry().registerDefaults().registerCollectionDefaults();

    private List<Cerealizer> cerealizers = new ArrayList<>();

    public CerealizeRegistry registerDefaults() {
        return registerDefaults(null);
    }

    public CerealizeRegistry registerDefaults(List<Class> shortened) {
        register(
                new PrimitiveSerializer(),
                new EnumSerializer(),
                new JavaTypeSerializer(),
                new ClassSerializer(shortened),
                new UUIDSerializer(),
                new PrimitiveArraySerializer(),
                new AnnotatedSerializer()
        );
        return this;
    }

    public CerealizeRegistry registerCollectionDefaults() {
        register(
                new GeneralArraySerializer(),
                new GeneralCollectionSerializer(),
                new GeneralMapSerializer()
        );
        return this;
    }

    public void register(Cerealizer... cerealizers) {
        Collections.addAll(this.cerealizers, cerealizers);
    }

    public Optional<Cerealizer> getFor(Object obj) {
        return this.cerealizers.stream().filter(c -> c.accepts(obj)).findFirst();
    }

    public Optional<Cerealizer> getFor(JavaType target, CerealNode node) {
        return this.cerealizers.stream().filter(c -> c.accepts(target, node)).findFirst();
    }

}
