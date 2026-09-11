package de.fovea.puppet.runtime;

public enum PuppetRuntimeType {
    AUTO("Auto"),
    PDK("PDK"),
    PUPPET_AGENT("Puppet Agent"),
    CUSTOM("Custom");

    private final String displayName;

    PuppetRuntimeType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static PuppetRuntimeType from(String value) {
        if (value == null || value.isBlank()) return AUTO;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return AUTO;
        }
    }
}
