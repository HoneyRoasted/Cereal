package io.github.honeyroasted.cereal.serialization;

import honeyroasted.javatype.JavaType;
import io.github.honeyroasted.cereal.tree.CerealNode;

public interface Cerealizer<T> {

    boolean accepts(Object obj);

    boolean accepts(JavaType target, CerealNode node);

    T deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache);

    CerealNode serialize(T obj, CerealizeRegistry registry, NodeCache cache);

}
