import io.github.honeyroasted.cereal.annotation.CerealConstructor;
import io.github.honeyroasted.cereal.annotation.CerealProperty;
import io.github.honeyroasted.cereal.annotation.CerealSetter;
import io.github.honeyroasted.cereal.annotation.Cerealizable;
import io.github.honeyroasted.cereal.serialization.Cereal;
import io.github.honeyroasted.cereal.serialization.CerealizeRegistry;
import io.github.honeyroasted.cereal.tree.CerealNode;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Thing t = new Thing("hello", 5, new Thing("lol", 43, null));

        CerealNode node = Cereal.serializeTree(t, CerealizeRegistry.GLOBAL);
        System.out.println(CerealNode.flatToString(node));

        Thing thing = node.deserialize(Thing.class);
        System.out.println(thing);
    }

    @Cerealizable(useBaseType = true)
    public static class Thing {
        private String name;
        private int yay;
        private Thing thing;

        public Thing(String name, int yay, Thing thing) {
            this.name = name;
            this.yay = yay;
            this.thing = thing;
        }

        @CerealConstructor
        public Thing() { }

        @CerealProperty("name")
        public String getName() {
            return this.name;
        }

        @CerealSetter("name")
        public void setName(String name) {
            this.name = name;
        }

        @CerealProperty("yay")
        public int getYay() {
            return this.yay;
        }

        @CerealSetter("yay")
        public void setYay(int yay) {
            this.yay = yay;
        }

        @CerealProperty("thing")
        public Thing getThing() {
            return this.thing;
        }

        @CerealSetter("thing")
        public void setThing(Thing thing) {
            this.thing = thing;
        }

        @Override
        public String toString() {
            return "Thing{" +
                    "name='" + name + '\'' +
                    ", yay=" + yay +
                    ", thing=" + thing +
                    '}';
        }
    }

}
