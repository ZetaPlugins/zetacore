package com.zetaplugins.zetacore.di;

/**
 * Supplies instances for types the {@link ServiceRegistry} cannot create on its own.
 * <p>
 * Resolvers are asked before the registry creates a new instance. The first non-null result is
 * stored in the registry as a singleton for the requested type.
 *
 * <pre>
 * registry.addResolver(type -&gt; type == Clock.class ? Clock.systemUTC() : null);
 * </pre>
 */
@FunctionalInterface
public interface InstanceResolver {
    /**
     * Resolves an instance for the given type.
     * @param type The requested type.
     * @return An instance assignable to the type, or null if this resolver does not handle it.
     */
    Object resolve(Class<?> type);
}
