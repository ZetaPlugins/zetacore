package com.zetaplugins.zetacore.services.config;

import com.zetaplugins.zetacore.annotations.PluginConfig;
import com.zetaplugins.zetacore.annotations.NestedConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ConfigMapper {

    public static String toFileName(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(PluginConfig.class)) {
            throw new IllegalArgumentException("Class " + configClass.getName() + " is not annotated with @ConfigProperties");
        }
        PluginConfig annotation = configClass.getAnnotation(PluginConfig.class);
        return annotation.value();
    }

    /**
     * Map values from a FileConfiguration into a new instance of the provided configClass.
     *
     * @param fileConfiguration the FileConfiguration containing configuration values
     * @param configClass the class to map the configuration into
     * @return a new instance of configClass populated with configuration values
     * @throws ConfigMappingException if mapping fails
     */
    public static <T> T map(FileConfiguration fileConfiguration, Class<T> configClass) throws ConfigMappingException {
        try {
            T instance = configClass.getConstructor().newInstance();
            mapSection((ConfigurationSection) fileConfiguration, instance);
            return instance;
        } catch (Exception e) {
            throw new ConfigMappingException("Failed to map configuration to class " + configClass.getName(), e);
        }
    }

    /**
     * Map values from a ConfigurationSection into the provided instance.
     *
     * @param section the ConfigurationSection containing configuration values
     * @param instance the instance to populate
     * @throws Exception if mapping fails
     */
    private static <T> void mapSection(ConfigurationSection section, T instance) throws Exception {
        Class<?> configClass = instance.getClass();

        for (Field field : configClass.getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            // Nested objects annotated with @NestedConfig
            if (fieldType.isAnnotationPresent(NestedConfig.class)) {
                ConfigurationSection nestedSection = section.getConfigurationSection(field.getName());
                if (nestedSection == null) continue;
                Object nestedInstance = fieldType.getConstructor().newInstance();
                mapSection(nestedSection, nestedInstance);
                field.set(instance, nestedInstance);
                continue;
            }

            // Lists
            if (isListType(fieldType)) {
                Object rawListObj = section.get(field.getName());
                if (rawListObj instanceof List<?> rawList) {
                    List<Object> mappedList = convertList(field.getGenericType(), rawList, field.getName());
                    field.set(instance, mappedList);
                } else if (rawListObj == null) {
                    // nothing present - skip
                } else {
                    throw new ConfigMappingException("Field '" + field.getName() + "' expected a List but got " + rawListObj.getClass().getName());
                }
                continue;
            }

            // Maps
            if (isMapType(fieldType)) {
                // raw can be a ConfigurationSection or a Map/LinkedHashMap
                Object rawMap = section.get(field.getName());
                ConfigurationSection rawSection = section.getConfigurationSection(field.getName());
                if (rawSection != null) {
                    Map<?, ?> mapped = convertMap(field.getGenericType(), rawSection, field.getName());
                    field.set(instance, mapped);
                } else if (rawMap instanceof LinkedHashMap<?, ?> || rawMap instanceof Map<?, ?>) {
                    Map<?, ?> mapped = convertMap(field.getGenericType(), rawMap, field.getName());
                    field.set(instance, mapped);
                } else if (rawMap == null) {
                    // nothing present - skip
                } else {
                    throw new ConfigMappingException("Field '" + field.getName() + "' expected a Map but got " + rawMap.getClass().getName());
                }
                continue;
            }

            // Primitive / String / basic types
            if (isSimpleType(fieldType)) {
                Object value = section.get(field.getName());
                if (value != null) {
                    field.set(instance, value);
                }
                continue;
            }

            throw new ConfigMappingException(
                    "Cannot map field '" + field.getName() + "' of type "
                            + fieldType.getName() + ". Field must be annotated with @NestedConfig or be a primitive/List/Map/String."
            );
        }
    }

    /**
     * Map values from a LinkedHashMap (YAML parser output) into the provided instance.
     *
     * @param map the LinkedHashMap containing configuration values
     * @param instance the instance to populate
     * @throws Exception if mapping fails
     */
    private static void mapFromMap(LinkedHashMap<?, ?> map, Object instance) throws Exception {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = map.get(field.getName());
            if (value == null) continue;

            Class<?> fieldType = field.getType();

            if (fieldType.isAnnotationPresent(NestedConfig.class) && value instanceof LinkedHashMap<?, ?> nestedMap) {
                Object nestedInstance = fieldType.getConstructor().newInstance();
                mapFromMap(nestedMap, nestedInstance);
                field.set(instance, nestedInstance);

            } else if (isListType(fieldType) && value instanceof List<?> rawList) {
                List<Object> mappedList = convertList(field.getGenericType(), rawList, field.getName());
                field.set(instance, mappedList);

            } else if (isMapType(fieldType) && value instanceof LinkedHashMap<?, ?> rawLinkedMap) {
                Map<?, ?> mappedMap = convertMap(field.getGenericType(), rawLinkedMap, field.getName());
                field.set(instance, mappedMap);

            } else if (isSimpleType(fieldType)) {
                field.set(instance, value);

            } else {
                throw new ConfigMappingException(
                        "Cannot map field '" + field.getName() + "' of type "
                                + fieldType.getName() + ". Field must be annotated with @NestedConfig or be a primitive/List/Map/String."
                );
            }
        }
    }

    /**
     * Convert a raw list (from YAML parser / LinkedHashMap representation) into a mapped list.<br/>
     * Supports:<br/>
     *  - Lists of primitives / String / Number / Boolean (keeps items as-is)<br/>
     *  - Lists of @NestedConfig classes where items are LinkedHashMap or ConfigurationSection<br/>
     *
     * @param genericType the generic type of the field (e.g. List<MyType>)
     * @param rawList the raw list object from YAML/Configuration
     * @param fieldName name of the field (used for error messages)
     * @return a newly constructed and mapped List<Object>
     * @throws Exception if mapping fails
     */
    private static List<Object> convertList(Type genericType, List<?> rawList, String fieldName) throws Exception {
        List<Object> mappedList = new ArrayList<>();

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type listType = parameterizedType.getActualTypeArguments()[0];
            Class<?> listClass = listType instanceof Class<?> c ? c : Object.class;

            for (Object item : rawList) {
                if (listClass.isAnnotationPresent(NestedConfig.class)) {
                    if (item instanceof LinkedHashMap<?, ?> itemMap) {
                        Object nestedInstance = listClass.getConstructor().newInstance();
                        mapFromMap(itemMap, nestedInstance);
                        mappedList.add(nestedInstance);
                    } else if (item instanceof ConfigurationSection itemSection) {
                        Object nestedInstance = listClass.getConstructor().newInstance();
                        mapSection(itemSection, nestedInstance);
                        mappedList.add(nestedInstance);
                    } else {
                        throw new ConfigMappingException(
                                "Cannot map list item for field '" + fieldName + "': unsupported item type "
                                        + (item == null ? "null" : item.getClass().getName())
                        );
                    }
                } else if (isSimpleType(listClass)) {
                    mappedList.add(item);
                } else {
                    throw new ConfigMappingException(
                            "Cannot map list item of type '" + listClass.getName() + "' in field '"
                                    + fieldName + "'. List item type must be annotated with @NestedConfig or be a primitive/String."
                    );
                }
            }
        } else {
            // raw list, so just add all items as is
            mappedList.addAll(rawList);
        }

        return mappedList;
    }

    /**
     * Convert a raw map (ConfigurationSection or LinkedHashMap/Map) into a mapped Map.<br/>
     * Supports:
     *  - Map<String, Primitive/String/Number/Boolean>
     *  - Map<String, @NestedConfig> where each value is a LinkedHashMap or ConfigurationSection
     *
     * @param genericType the generic type of the field (e.g. Map<String,MyType>)
     * @param rawMap the raw map object from YAML/Configuration (either ConfigurationSection or Map)
     * @param fieldName name of the field (used for error messages)
     * @return a newly constructed and mapped Map<Object,Object> (LinkedHashMap to preserve order)
     * @throws Exception if mapping fails
     */
    private static Map<Object, Object> convertMap(Type genericType, Object rawMap, String fieldName) throws Exception {
        Map<Object, Object> result = new LinkedHashMap<>();

        Class<?> keyClass = Object.class;
        Class<?> valueClass = Object.class;

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length == 2) {
                keyClass = typeArgs[0] instanceof Class<?> kc ? (Class<?>) typeArgs[0] : Object.class;
                valueClass = typeArgs[1] instanceof Class<?> vc ? (Class<?>) typeArgs[1] : Object.class;
            }
        }

        final Class<?> finalValueClass = valueClass;
        final String finalFieldName = fieldName;

        java.util.function.Function<Object, Object> mapValueMapper = new java.util.function.Function<>() {
            @Override
            public Object apply(Object rawValue) {
                try {
                    if (finalValueClass.isAnnotationPresent(NestedConfig.class)) {
                        if (rawValue instanceof LinkedHashMap<?, ?> itemMap) {
                            Object nestedInstance = finalValueClass.getConstructor().newInstance();
                            mapFromMap(itemMap, nestedInstance);
                            return nestedInstance;
                        } else if (rawValue instanceof ConfigurationSection itemSection) {
                            Object nestedInstance = finalValueClass.getConstructor().newInstance();
                            mapSection(itemSection, nestedInstance);
                            return nestedInstance;
                        } else {
                            throw new ConfigMappingException(
                                    "Cannot map map value for field '" + finalFieldName
                                            + "': unsupported item type " + (rawValue == null ? "null" : rawValue.getClass().getName()));
                        }
                    } else if (isSimpleType(finalValueClass) || finalValueClass == Object.class) {
                        return rawValue;
                    } else {
                        throw new ConfigMappingException(
                                "Cannot map map value of type '" + finalValueClass.getName() + "' in field '"
                                        + finalFieldName + "'. Map value type must be annotated with @NestedConfig or be a primitive/String."
                        );
                    }
                } catch (Exception e) {
                    if (e instanceof RuntimeException) throw (RuntimeException) e;
                    throw new RuntimeException(e);
                }
            }
        };

        if (rawMap instanceof ConfigurationSection section) {
            // iterate keys
            for (String key : section.getKeys(false)) {
                Object rawValue = section.get(key);
                Object mappedValue = mapValueMapper.apply(rawValue);
                Object mappedKey = convertMapKey(key, keyClass, fieldName);
                result.put(mappedKey, mappedValue);
            }
        } else if (rawMap instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object rawKey = entry.getKey();
                Object rawValue = entry.getValue();
                Object mappedKey = convertMapKey(rawKey, keyClass, fieldName);
                Object mappedValue = mapValueMapper.apply(rawValue);
                result.put(mappedKey, mappedValue);
            }
        } else {
            throw new ConfigMappingException("Field '" + fieldName + "' expected a map-like structure but got " + (rawMap == null ? "null" : rawMap.getClass().getName()));
        }

        return result;
    }

    private static Object convertMapKey(Object rawKey, Class<?> keyClass, String fieldName) throws ConfigMappingException {
        if (keyClass == String.class || keyClass == Object.class) {
            return String.valueOf(rawKey);
        } else if (keyClass.isAssignableFrom(rawKey.getClass())) {
            return rawKey;
        } else {
            // try basic conversions for common key types (e.g., Integer, Long)
            try {
                String keyStr = String.valueOf(rawKey);
                if (keyClass == Integer.class || keyClass == int.class) return Integer.valueOf(keyStr);
                if (keyClass == Long.class || keyClass == long.class) return Long.valueOf(keyStr);
                if (keyClass == Short.class || keyClass == short.class) return Short.valueOf(keyStr);
                if (keyClass == Byte.class || keyClass == byte.class) return Byte.valueOf(keyStr);
                if (keyClass == Double.class || keyClass == double.class) return Double.valueOf(keyStr);
                if (keyClass == Float.class || keyClass == float.class) return Float.valueOf(keyStr);
                if (keyClass == Boolean.class || keyClass == boolean.class) return Boolean.valueOf(keyStr);
            } catch (Exception ignored) { }
            throw new ConfigMappingException("Cannot convert map key '" + rawKey + "' to required key type '" + keyClass.getName() + "' for field '" + fieldName + "'");
        }
    }

    /**
     * Check if the provided type is a primitive, String, Number, Boolean
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class;
    }

    private static boolean isListType(Class<?> type) {
        return List.class.isAssignableFrom(type);
    }

    private static boolean isMapType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }
}