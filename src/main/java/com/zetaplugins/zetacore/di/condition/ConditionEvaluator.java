package com.zetaplugins.zetacore.di.condition;

import com.zetaplugins.zetacore.di.ServiceException;
import com.zetaplugins.zetacore.di.ServiceRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Evaluates {@link Conditional}, {@link ConditionalOnPlugin} and {@link ConditionalOnMissingPlugin} on classes and methods.
 * The result of each {@link Condition} class is cached. The evaluator is thread-safe.
 */
public class ConditionEvaluator {
    private final JavaPlugin plugin;
    private final ServiceRegistry registry;
    private final Map<Class<? extends Condition>, Boolean> results = new ConcurrentHashMap<>();

    /**
     * Creates an evaluator.
     * @param plugin The plugin, used to look up other plugins.
     * @param registry The registry used to create conditions with dependency injection, or null to
     *                 create them with their no-argument constructor.
     */
    public ConditionEvaluator(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    /**
     * Checks whether all conditions on the element match. Elements without conditions always match.
     * @param element The class or method to check.
     * @return True if the element is active.
     */
    public boolean matches(AnnotatedElement element) {
        return findFailedCondition(element) == null;
    }

    /**
     * Finds the first condition on the element that does not match.
     * @param element The class or method to check.
     * @return A description of the failed condition, or null if all conditions match.
     */
    public String findFailedCondition(AnnotatedElement element) {
        ConditionalOnPlugin onPlugin = element.getAnnotation(ConditionalOnPlugin.class);
        if (onPlugin != null) {
            for (String name : onPlugin.value()) {
                if (!isPluginPresent(name)) return "@ConditionalOnPlugin(\"" + name + "\")";
            }
        }

        ConditionalOnMissingPlugin onMissingPlugin = element.getAnnotation(ConditionalOnMissingPlugin.class);
        if (onMissingPlugin != null) {
            for (String name : onMissingPlugin.value()) {
                if (isPluginPresent(name)) return "@ConditionalOnMissingPlugin(\"" + name + "\")";
            }
        }

        Conditional conditional = element.getAnnotation(Conditional.class);
        if (conditional != null) {
            for (Class<? extends Condition> conditionClass : conditional.value()) {
                if (!evaluate(conditionClass)) return "@Conditional(" + conditionClass.getSimpleName() + ")";
            }
        }
        return null;
    }

    private boolean isPluginPresent(String name) {
        return plugin.getServer().getPluginManager().getPlugin(name) != null;
    }

    private boolean evaluate(Class<? extends Condition> conditionClass) {
        Boolean cached = results.get(conditionClass);
        if (cached != null) return cached;

        boolean result;
        try {
            result = instantiate(conditionClass).matches();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to evaluate condition " + conditionClass.getName(), e);
        }
        Boolean previous = results.putIfAbsent(conditionClass, result);
        return previous != null ? previous : result;
    }

    private Condition instantiate(Class<? extends Condition> conditionClass) throws ReflectiveOperationException {
        if (registry != null) return registry.create(conditionClass);
        Constructor<? extends Condition> constructor = conditionClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
