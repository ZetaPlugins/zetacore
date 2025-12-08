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

public class ConfigMapper {

    public static String toFileName(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(PluginConfig.class)) {
            throw new IllegalArgumentException("Class " + configClass.getName() + " is not annotated with @ConfigProperties");
        }
        PluginConfig annotation = configClass.getAnnotation(PluginConfig.class);
        return annotation.value();
    }

    public static <T> T map(FileConfiguration fileConfiguration, Class<T> configClass) {
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
                            + fieldType.getName() + ". Field must be annotated with @NestedConfig or be a primitive/List/String."
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

            } else if (isSimpleType(fieldType)) {
                field.set(instance, value);

            } else {
                throw new ConfigMappingException(
                        "Cannot map field '" + field.getName() + "' of type "
                                + fieldType.getName() + ". Field must be annotated with @NestedConfig or be a primitive/List/String."
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
     * Check if the provided type is a primitive, String, Number, Boolean
     */
    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class;
    }

    private static boolean isListType(Class<?> type) {
        return List.class.isAssignableFrom(type);
    }
}