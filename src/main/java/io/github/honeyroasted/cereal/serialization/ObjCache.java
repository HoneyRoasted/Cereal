package io.github.honeyroasted.cereal.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ObjCache {
    private Map<Integer, Object> objects = new HashMap<>();
    private Map<Integer, List<BiConsumer<Object, ObjCache>>> injectors = new HashMap<>();

    public Object get(int key) {
        return objects.get(key);
    }

    public Object get(Object key) {
        return get(System.identityHashCode(key));
    }

    public void put(Object key, Object node) {
        put(System.identityHashCode(key), node);
    }

    public void put(int key, Object node) {
        objects.put(key, node);
    }

    public boolean containsKey(int id) {
        return this.objects.containsKey(id);
    }

    public boolean containsKey(Object obj) {
        return containsKey(System.identityHashCode(obj));
    }

    public <T> void addInjector(T obj, BiConsumer<T, ObjCache> injector) {
        injectors.computeIfAbsent(System.identityHashCode(obj), i -> new ArrayList<>()).add((BiConsumer<Object, ObjCache>) injector);
    }

    public boolean doneInjecting() {
        return this.injectors.isEmpty();
    }

    public void inject() {
        this.injectors.forEach((k, v) -> v.removeIf(injector -> {
            if (this.containsKey(k)) {
                injector.accept(this.get(k), this);
                return true;
            } else {
                return false;
            }
        }));
    }

}
