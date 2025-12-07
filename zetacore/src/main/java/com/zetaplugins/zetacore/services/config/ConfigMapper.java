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

    /**
     * Map a FileConfiguration to an instance of the specified configuration class.
     *
     * @param fileConfiguration The FileConfiguration to map from.
     * @param configClass The class to map to.
     * @param <T> The type of the configuration class.
     * @return An instance of the configuration class populated with data from the FileConfiguration.
     */
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
     * Recursively map a ConfigurationSection to an instance of the specified class.
     *
     * @param section The ConfigurationSection to map from.
     * @param instance The instance to populate.
     * @param <T> The type of the instance.
     * @throws Exception If mapping fails.
     */
    private static <T> void mapSection(ConfigurationSection section, T instance) throws Exception {
        Class<?> configClass = instance.getClass();

        for (Field field : configClass.getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            // Nested objects annotated with @NestedConfig
            if (fieldType.isAnnotationPresent(NestedConfig.class)) {
                ConfigurationSection nestedSection = section.getConfigurationSection(field.getName());
                if (nestedSection == null) continue; // no data to map
                Object nestedInstance = fieldType.getConstructor().newInstance();
                mapSection(nestedSection, nestedInstance);
                field.set(instance, nestedInstance);
                continue;
            }

            // Lists
            if (List.class.isAssignableFrom(fieldType)) {
                Object rawListObj = section.get(field.getName());
                if (rawListObj instanceof List<?> rawList) {
                    List<Object> mappedList = new ArrayList<>();
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType parameterizedType) {
                        Type listType = parameterizedType.getActualTypeArguments()[0];
                        Class<?> listClass = listType instanceof Class<?> c ? c : Object.class;

                        for (Object item : rawList) {
                            if (listClass.isAnnotationPresent(NestedConfig.class)) {
                                if (item instanceof ConfigurationSection itemSection) {
                                    Object nestedInstance = listClass.getConstructor().newInstance();
                                    mapSection(itemSection, nestedInstance);
                                    mappedList.add(nestedInstance);
                                } else if (item instanceof LinkedHashMap<?, ?> map) {
                                    Object nestedInstance = listClass.getConstructor().newInstance();
                                    mapFromMap(map, nestedInstance);
                                    mappedList.add(nestedInstance);
                                }
                            } else if (isSimpleType(listClass)) {
                                mappedList.add(item); // primitive or String
                            } else {
                                throw new ConfigMappingException(
                                        "Cannot map list item of type '" + listClass.getName() + "' in field '"
                                                + field.getName() + "'. List item type must be annotated with @NestedConfig or be a primitive/String."
                                );
                            }
                        }
                    } else {
                        mappedList.addAll(rawList); // raw list
                    }
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
            } else if (isSimpleType(fieldType) || List.class.isAssignableFrom(fieldType)) {
                field.set(instance, value);
            } else {
                throw new ConfigMappingException(
                        "Cannot map field '" + field.getName() + "' of type "
                                + fieldType.getName() + ". Field must be annotated with @NestedConfig or be a primitive/List/String."
                );
            }
        }
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type == String.class || Number.class.isAssignableFrom(type) || type == Boolean.class;
    }
}