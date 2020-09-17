package io.github.honeyroasted.cereal.serialization.serializers;

import honeyroasted.javatype.JavaType;
import honeyroasted.javatype.JavaTypes;
import io.github.honeyroasted.cereal.annotation.CerealConstructor;
import io.github.honeyroasted.cereal.annotation.CerealProperty;
import io.github.honeyroasted.cereal.annotation.CerealSetter;
import io.github.honeyroasted.cereal.annotation.Cerealizable;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.serialization.Cerealizer;
import io.github.honeyroasted.cereal.serialization.NodeCache;
import io.github.honeyroasted.cereal.serialization.ObjCache;
import io.github.honeyroasted.cereal.serialization.SerializationException;
import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.NodeType;
import io.github.honeyroasted.cereal.tree.MapNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class AnnotatedSerializer implements Cerealizer<Object> {

    @Override
    public boolean accepts(Object obj) {
        return obj != null && obj.getClass().isAnnotationPresent(Cerealizable.class);
    }

    @Override
    public boolean accepts(JavaType target, CerealNode node) {
        return target.getType().isAnnotationPresent(Cerealizable.class) && node.type() == NodeType.MAP;
    }

    @Override
    public Object deserialize(JavaType target, CerealNode node, CerealizeRegistry registry, ObjCache cache) {
        Class<?> cls = target.getType();
        Cerealizable cerealizable = cls.getAnnotation(Cerealizable.class);

        Constructor<?> constructor = null;
        for (Constructor<?> c : cls.getConstructors()) {
            if (c.isAnnotationPresent(CerealConstructor.class)) {
                constructor = c;
                break;
            }
        }

        if (constructor == null) {
            throw new SerializationException("No constructor annotated with @CerealConstructor found in class: " + cls.getName());
        }

        Object[] params = new Object[constructor.getParameterCount()];
        int index = 0;
        for (Parameter parameter : constructor.getParameters()) {
            CerealSetter setter = parameter.getAnnotation(CerealSetter.class);
            if (setter != null) {
                String name = setter.value();
                JavaType type = null;

                for (Field field : cls.getDeclaredFields()) {
                    if (field.isAnnotationPresent(CerealProperty.class)) {
                        CerealProperty property = field.getAnnotation(CerealProperty.class);
                        if (name.equals(property.value()) || (property.value().equals(CerealProperty.FIELD_NAME) && name.equals(field.getName()))) {
                            if (cerealizable.useBaseType() || property.useBaseType()) {
                                type = JavaTypes.of(field.getGenericType());
                            }

                            if (name.equals(CerealProperty.FIELD_NAME)) {
                                name = parameter.getName();
                            }
                            break;
                        }
                    }
                }

                if (type == null) {
                    for (Method method : cls.getMethods()) {
                        if (method.isAnnotationPresent(CerealProperty.class)) {
                            CerealProperty property = method.getAnnotation(CerealProperty.class);
                            if (name.equals(property.value()) || (property.value().equals(CerealProperty.FIELD_NAME) && name.equals(method.getName()))) {
                                if (cerealizable.useBaseType() || property.useBaseType()) {
                                    type = JavaTypes.of(method.getGenericReturnType());
                                }

                                if (name.equals(CerealProperty.FIELD_NAME)) {
                                    name = parameter.getName();
                                }
                                break;
                            }
                        }
                    }
                }

                if (type == null) {
                    params[index++] = Cereal.deserializeIsolated(node.mapView().get(name).get(), registry, cache);
                } else {
                    params[index++] = Cereal.deserialize(type, node.mapView().get(name).get(), registry, cache);
                }

            } else {
                throw new SerializationException("No @CerealSetter annotation on @CerealConstructor parameter: " + parameter.getName() + " in class: " + cls.getName());
            }
        }

        try {
            Object obj = constructor.newInstance(params);
            cache.put(node, obj);

            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(CerealProperty.class)) {
                    CerealProperty property = field.getAnnotation(CerealProperty.class);
                    String name = property.value().equals(CerealProperty.FIELD_NAME) ? field.getName() : property.value();
                    if (property.reflective() || cerealizable.reflective()) {
                        field.setAccessible(true);
                        Object val;
                        if (property.useBaseType() || cerealizable.useBaseType()) {
                            val = Cereal.deserialize(JavaTypes.of(field.getGenericType()), node.mapView().get(name).get(), registry, cache);
                        } else {
                            val = Cereal.deserializeIsolated(node.mapView().get(name).get(), registry, cache);
                        }

                        try {
                            field.set(obj, val);
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            throw new SerializationException("Failed to set field: " + field.getName() + " on class: " + cls.getName(), e);
                        }
                    }
                }
            }

            for (Method method : cls.getMethods()) {
                if (method.isAnnotationPresent(CerealSetter.class) && method.getParameterCount() == 1) {
                    CerealSetter setter = method.getAnnotation(CerealSetter.class);
                    String name = setter.value();
                    JavaType type = null;

                    for (Field field : cls.getDeclaredFields()) {
                        if (field.isAnnotationPresent(CerealProperty.class)) {
                            CerealProperty property = field.getAnnotation(CerealProperty.class);
                            if (name.equals(property.value()) || (property.value().equals(CerealProperty.FIELD_NAME) && name.equals(field.getName()))) {
                                if (cerealizable.useBaseType() || property.useBaseType()) {
                                    type = JavaTypes.of(field.getGenericType());
                                }

                                if (name.equals(CerealProperty.FIELD_NAME)) {
                                    name = method.getName();
                                }
                                break;
                            }
                        }
                    }

                    if (type == null) {
                        for (Method m : cls.getMethods()) {
                            if (m.isAnnotationPresent(CerealProperty.class)) {
                                CerealProperty property = m.getAnnotation(CerealProperty.class);
                                if (name.equals(property.value()) || (property.value().equals(CerealProperty.FIELD_NAME) && name.equals(method.getName()))) {
                                    if (cerealizable.useBaseType() || property.useBaseType()) {
                                        type = JavaTypes.of(m.getGenericReturnType());
                                    }

                                    if (name.equals(CerealProperty.FIELD_NAME)) {
                                        name = method.getName();
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    Object val;
                    if (type == null) {
                        val = Cereal.deserializeIsolated(node.mapView().get(name).get(), registry, cache);
                    } else {
                        val = Cereal.deserialize(JavaTypes.of(method.getGenericParameterTypes()[0]), node.mapView().get(name).get(), registry, cache);
                    }

                    try {
                        method.invoke(obj, val);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new SerializationException("Failed to invoke method: " + method.getName() + " on class: " + cls.getName(), e);
                    }
                }
            }

            return obj;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new SerializationException("Failed to invoke constructor on class: " + cls.getName(), e);
        }
    }

    @Override
    public CerealNode serialize(Object obj, CerealizeRegistry registry, NodeCache cache) {
        Class<?> cls = obj.getClass();
        Cerealizable cerealizable = cls.getAnnotation(Cerealizable.class);

        MapNode node = MapNode.create(cls);
        cache.put(obj, node);

        for (Field field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(CerealProperty.class)) {
                CerealProperty property = field.getAnnotation(CerealProperty.class);
                field.setAccessible(true);

                String name = property.value().equals(CerealProperty.FIELD_NAME) ? field.getName() : property.value();
                try {
                    Object val = field.get(obj);
                    if (property.useBaseType() || cerealizable.useBaseType()) {
                        node.put(name, Cereal.serialize(val, registry, cache));
                    } else {
                        node.put(name, Cereal.serializeIsolated(val, registry, cache));
                    }
                } catch (IllegalAccessException e) {
                    throw new SerializationException("Cannot access field: " + field.getName() + " in class: " + cls.getName(), e);
                }
            }
        }

        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(CerealProperty.class) && method.getParameterCount() == 0) {
                CerealProperty property = method.getAnnotation(CerealProperty.class);
                String name = property.value().equals(CerealProperty.FIELD_NAME) ? method.getName() : property.value();
                try {
                    Object val = method.invoke(obj);
                    if (property.useBaseType() || cerealizable.useBaseType()) {
                        node.put(name, Cereal.serialize(val, registry, cache));
                    } else {
                        node.put(name, Cereal.serializeIsolated(val, registry, cache));
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new SerializationException("Failed to invoke method: " + method.getName() + " in class " + cls.getName(), e);
                }
            }
        }

        return node;
    }

}
