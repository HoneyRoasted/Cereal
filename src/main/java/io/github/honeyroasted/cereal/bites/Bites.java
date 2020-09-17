package io.github.honeyroasted.cereal.bites;

import io.github.honeyroasted.cereal.tree.CerealNode;
import io.github.honeyroasted.cereal.tree.ListNode;
import io.github.honeyroasted.cereal.tree.NodeType;
import io.github.honeyroasted.cereal.tree.MapNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Bites {
    public static final byte NULL = 0;

    public static final byte BOOL = 1;

    public static final byte BYTE = 2;
    public static final byte SHORT = 3;
    public static final byte CHAR = 4;
    public static final byte INT = 5;
    public static final byte LONG = 6;
    public static final byte FLOAT = 7;
    public static final byte DOUBLE = 8;
    public static final byte STRING = 9;

    public static final byte LIST = 10;
    public static final byte MAP = 11;

    public static final byte HOMOLIST = 12;
    public static final byte HOMOMAP = 13;

    public static byte[] serialize(CerealNode node) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serialize(node, new DataOutputStream(bos));
        return bos.toByteArray();
    }

    public static CerealNode deserialize(byte[] arr) throws IOException {
        return deserialize(new DataInputStream(new ByteArrayInputStream(arr)));
    }

    public static CerealNode deserialize(DataInputStream stream) throws IOException {
        return deserialize(stream, stream.readByte());
    }

    public static void serialize(CerealNode node, DataOutputStream stream) throws IOException {
        serialize(node, stream, false);
    }

    public static CerealNode deserialize(DataInputStream stream, byte id) throws IOException {
        boolean homo = id == HOMOLIST || id == HOMOMAP;
        if (id == LIST || id == HOMOLIST) {
            ListNode list = ListNode.create();
            byte childId = NULL;
            if (homo) {
                childId = stream.readByte();
            }

            int size = readSize(stream);
            for (int i = 0; i < size; i++) {
                if (!homo) {
                    childId = stream.readByte();
                }
                list.add(deserialize(stream, childId));
            }

            return list;
        } else if (id == MAP || id == HOMOMAP) {
            MapNode map = MapNode.create();
            byte childId = NULL;
            if (homo) {
                childId = stream.readByte();
            }

            int size = readSize(stream);
            for (int i = 0; i < size; i++) {
                String key = readStr(stream);
                if (!homo) {
                    childId = stream.readByte();
                }
                map.putExactly(key, deserialize(stream, childId));
            }

            return map;
        } else {
            return CerealNode.of(readPrim(stream, id));
        }
    }

    public static void serialize(CerealNode node, DataOutputStream stream, boolean skipId) throws IOException {
        boolean homo = isHomo(node);
        if(!skipId) stream.write(getId(node));

        if (node.type() == NodeType.PRIMITIVE) {
            Object obj = node.get();
            serializePrim(obj, stream);
        } else if (node.type() == NodeType.LIST) {
            if (homo) {
                stream.write(node.stream().findFirst().map(Bites::getId).orElse(NULL));
            }

            writeSize(node.size(), stream);
            for (CerealNode child : node) {
                serialize(child, stream, homo);
            }
        } else if (node.type() == NodeType.MAP) {
            if (homo) {
                stream.write(node.stream().findFirst().map(Bites::getId).orElse(NULL));
            }

            writeSize(node.size(), stream);
            for (String key : node.mapView().keys()) {
                CerealNode child = node.mapView().get(key).get();
                writeStr(key, stream);
                serialize(child, stream, homo);
            }
        }
    }

    private static void serializePrim(Object obj, DataOutputStream stream) throws IOException {
        if (obj != null) {
            if (obj instanceof Boolean) {
                stream.writeBoolean((Boolean) obj);
            } else if (obj instanceof Byte) {
                stream.write((Byte) obj);
            } else if (obj instanceof Short) {
                stream.writeShort((Short) obj);
            } else if (obj instanceof Character) {
                stream.writeShort((Character) obj);
            } else if (obj instanceof Integer) {
                stream.writeInt((Integer) obj);
            } else if (obj instanceof Long) {
                stream.writeLong((Long) obj);
            } else if (obj instanceof Float) {
                stream.writeFloat((Float) obj);
            } else if (obj instanceof Double) {
                stream.writeDouble((Double) obj);
            } else if (obj instanceof String) {
                writeStr((String) obj, stream);
            } else {
                throw new IllegalArgumentException("Unknown primitive: " + obj);
            }
        }
    }

    private static Object readPrim(DataInputStream stream, byte primId) throws IOException {
        switch (primId) {
            case BYTE:
                return stream.readByte();
            case BOOL:
                return stream.readBoolean();
            case SHORT:
            case CHAR:
                return stream.readShort();
            case INT:
                return stream.readInt();
            case LONG:
                return stream.readLong();
            case FLOAT:
                return stream.readFloat();
            case DOUBLE:
                return stream.readDouble();
            case STRING:
                return readStr(stream);
            case NULL:
                return null;
            default:
                throw new IllegalArgumentException("Unknown id: " + primId);
        }
    }

    private static byte getId(CerealNode node) {
        if (node.type() == NodeType.LIST) {
            return isHomo(node) ? HOMOLIST : LIST;
        } else if (node.type() == NodeType.MAP) {
            return isHomo(node) ? HOMOMAP : MAP;
        } else {
            return getPrimId(node.get());
        }
    }

    private static byte getPrimId(Object obj) {
        if (obj == null) {
            return NULL;
        } else if (obj instanceof Byte) {
            return BYTE;
        } else if (obj instanceof Boolean) {
            return BOOL;
        } else if (obj instanceof Short) {
            return SHORT;
        } else if (obj instanceof Character) {
            return CHAR;
        } else if (obj instanceof Integer) {
            return INT;
        } else if (obj instanceof Long) {
            return LONG;
        } else if (obj instanceof Float) {
            return FLOAT;
        } else if (obj instanceof Double) {
            return DOUBLE;
        } else if (obj instanceof String) {
            return STRING;
        }
        return NULL;
    }

    private static boolean isHomo(CerealNode node) {
        byte id = -1;
        for (CerealNode child : node) {
            if (!child.isNull()) {
                if (id == -1) {
                    id = getId(child);
                } else if (id != getId(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void writeStr(String str, DataOutputStream stream) throws IOException {
        writeArr(str.getBytes(StandardCharsets.UTF_8), stream);
    }

    private static String readStr(DataInputStream stream) throws IOException {
        return new String(readArr(stream), StandardCharsets.UTF_8);
    }

    private static void writeArr(byte[] arr, DataOutputStream stream) throws IOException {
        writeSize(arr.length, stream);
        stream.write(arr);
    }

    private static byte[] readArr(DataInputStream stream) throws IOException {
        int size = readSize(stream);
        byte[] arr = new byte[size];
        stream.read(arr);
        return arr;
    }

    private static void writeSize(int size, DataOutputStream stream) throws IOException {
        stream.writeInt(size);
    }

    private static int readSize(DataInputStream stream) throws IOException {
        return stream.readInt();
    }

}
