package com.zetaplugins.zetacore.integration.papi;

/**
 * Customizes the PlaceholderAPI expansion that {@link com.zetaplugins.zetacore.ZetaCorePlugin} registers automatically.
 * <p>
 * Provide implementations as services or beans; all of them are applied before the expansion is registered.
 *
 * <pre>
 * {@literal @}Service
 * public class PlaceholderSettings implements PlaceholderExpansionCustomizer {
 *     {@literal @}Override
 *     public void customize(PapiExpansionService expansion) {
 *         expansion.setIdentifier("myplugin").setAuthor("Me");
 *     }
 * }
 * </pre>
 */
@FunctionalInterface
public interface PlaceholderExpansionCustomizer {
    /**
     * Customizes the expansion before it is registered.
     * @param expansion The expansion, with all discovered placeholders added.
     */
    void customize(PapiExpansionService expansion);
}
