package me.portmapping.trading.utils;

import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemStackBsonUtil {

    /** Serialize an ItemStack into a BSON-safe Document */
    public static Document serializeItemStack(ItemStack item) {
        Map<String, Object> serialized = item.serialize();
        return convertToBsonDocument(serialized);
    }

    /** Deserialize a Document into an ItemStack */
    public static ItemStack deserializeItemStack(Document doc) {
        Map<String, Object> map = convertToMap(doc);
        return ItemStack.deserialize(map);
    }

    /** Recursively convert Map<String, Object> into BSON-safe Document */
    @SuppressWarnings("unchecked")
    private static Document convertToBsonDocument(Map<String, Object> map) {
        Document doc = new Document();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                doc.append(entry.getKey(), convertToBsonDocument((Map<String, Object>) value));
            } else if (value instanceof List) {
                doc.append(entry.getKey(), convertToBsonList((List<Object>) value));
            } else if (isBsonCompatible(value)) {
                doc.append(entry.getKey(), value);
            } else {
                // fallback: convert unknown types to string
                doc.append(entry.getKey(), value.toString());
            }
        }
        return doc;
    }

    /** Recursively convert List<Object> into BSON-safe List */
    @SuppressWarnings("unchecked")
    private static List<Object> convertToBsonList(List<Object> list) {
        List<Object> newList = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                newList.add(convertToBsonDocument((Map<String, Object>) item));
            } else if (item instanceof List) {
                newList.add(convertToBsonList((List<Object>) item));
            } else if (isBsonCompatible(item)) {
                newList.add(item);
            } else {
                newList.add(item.toString());
            }
        }
        return newList;
    }

    /** Convert BSON Document back to Map<String, Object> for ItemStack.deserialize() */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertToMap(Document doc) {
        Map<String, Object> map = new HashMap<>();
        for (String key : doc.keySet()) {
            Object value = doc.get(key);
            if (value instanceof Document) {
                map.put(key, convertToMap((Document) value));
            } else if (value instanceof List) {
                map.put(key, convertToList((List<Object>) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /** Convert BSON List back to List<Object> */
    @SuppressWarnings("unchecked")
    private static List<Object> convertToList(List<Object> list) {
        List<Object> newList = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Document) {
                newList.add(convertToMap((Document) item));
            } else if (item instanceof List) {
                newList.add(convertToList((List<Object>) item));
            } else {
                newList.add(item);
            }
        }
        return newList;
    }

    /** Check if an object is BSON-compatible */
    private static boolean isBsonCompatible(Object o) {
        return o == null ||
               o instanceof String ||
               o instanceof Number ||
               o instanceof Boolean ||
               o instanceof Character ||
               o instanceof Date;
    }
}
