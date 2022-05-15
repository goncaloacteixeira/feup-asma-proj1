package agents;

import lombok.Getter;

/**
 * @param carServiceFare Price Per Unit
 */
public record EnvironmentPreferences(@Getter double carServiceFare) {}