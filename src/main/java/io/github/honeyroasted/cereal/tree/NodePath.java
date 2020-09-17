package io.github.honeyroasted.cereal.tree;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodePath {
    private List<PathElement> path;

    private NodePath(List<PathElement> elements) {
        this.path = elements;
    }

    public static NodePath of(Object... elements) {
        List<PathElement> path = new ArrayList<>();
        for (Object obj : elements) {
            if (obj instanceof Integer) {
                path.add(new PathElement((int) obj));
            } else {
                path.add(new PathElement(String.valueOf(obj)));
            }
        }
        return new NodePath(path);
    }

    private static class PathElement {
        public int index;
        public String key;

        public PathElement(String key) {
            this.key = key;
        }

        public PathElement(int index) {
            this.index = index;
        }

        public boolean isKey() {
            return key != null;
        }

    }

    Optional<CerealNode> query(CerealNode node) {
        for (PathElement element : this.path) {
            if (element.isKey()) {
                if (node.type() == NodeType.MAP) {
                    Optional<CerealNode> opt = node.mapView().get(element.key);
                    if (opt.isPresent()) {
                        node = opt.get();
                    } else {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            } else {
                if (node.type() == NodeType.LIST) {
                    Optional<CerealNode> opt = node.listView().get(element.index);
                    if (opt.isPresent()) {
                        node = opt.get();
                    } else {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            }
        }

        return Optional.ofNullable(node);
    }

}
