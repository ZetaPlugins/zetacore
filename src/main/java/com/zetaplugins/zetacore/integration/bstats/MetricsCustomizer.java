package com.zetaplugins.zetacore.integration.bstats;

/**
 * Adds custom charts to the bStats metrics that {@link com.zetaplugins.zetacore.ZetaCorePlugin} creates
 * when {@link com.zetaplugins.zetacore.annotation.ZetaPlugin#bstatsId()} is set.
 * <p>
 * Provide implementations as services or beans; all of them are applied.
 *
 * <pre>
 * {@literal @}Service
 * public class StorageMetrics implements MetricsCustomizer {
 *     private final StorageConfig config;
 *
 *     public StorageMetrics(StorageConfig config) {
 *         this.config = config;
 *     }
 *
 *     {@literal @}Override
 *     public void customize(Metrics metrics) {
 *         metrics.addCustomChart(new Metrics.SimplePie("storage_type", () -&gt; config.type));
 *     }
 * }
 * </pre>
 */
@FunctionalInterface
public interface MetricsCustomizer {
    /**
     * Adds charts to the metrics.
     * @param metrics The plugin's metrics.
     */
    void customize(Metrics metrics);
}
