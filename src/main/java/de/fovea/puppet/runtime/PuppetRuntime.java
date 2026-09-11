package de.fovea.puppet.runtime;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public record PuppetRuntime(
        @NotNull PuppetRuntimeType type,
        @Nullable Path installationDirectory,
        @Nullable PuppetCommand ruby,
        @Nullable PuppetCommand puppet,
        @Nullable PuppetCommand pdk,
        @Nullable PuppetCommand puppetLint,
        @Nullable PuppetCommand languageServer) {

    public @NotNull String description() {
        String location = installationDirectory == null ? "PATH / overrides" : installationDirectory.toString();
        return type + " (" + location + ")";
    }
}
