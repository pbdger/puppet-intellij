package de.consoluta.puppet.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import de.consoluta.puppet.settings.PuppetSettingsState;

import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;

/** Utilities for finding the Puppet module/workspace that contains a file. */
public final class PuppetProjectUtil {
    private PuppetProjectUtil() {}

    /**
     * Finds the nearest directory containing metadata.json, walking upward from the selected file.
     * Falls back to the IntelliJ project base directory.
     */
    public static @Nullable Path findModuleRoot(@NotNull Project project, @Nullable VirtualFile file) {
        String basePath = project.getBasePath();
        Path projectRoot = basePath == null ? null : Path.of(basePath).toAbsolutePath().normalize();

        if (file != null && file.isInLocalFileSystem()) {
            Path current = Path.of(file.getPath()).toAbsolutePath().normalize();
            if (!file.isDirectory()) current = current.getParent();

            while (current != null) {
                if (Files.isRegularFile(current.resolve("metadata.json"))) return current;
                if (projectRoot != null && current.equals(projectRoot)) break;
                current = current.getParent();
            }
        }
        return projectRoot;
    }

    /** Adds optional Puppet path settings to a Puppet CLI argument list. */
    public static void addPuppetPathArguments(@NotNull List<String> arguments, @NotNull PuppetSettingsState.StateData settings) {
        if (settings.modulePath != null && !settings.modulePath.isBlank()) {
            arguments.add("--modulepath");
            arguments.add(settings.modulePath.trim());
        }
        if (settings.environmentPath != null && !settings.environmentPath.isBlank()) {
            arguments.add("--environmentpath");
            arguments.add(settings.environmentPath.trim());
        }
    }

    /** Converts optional path settings to puppet-editor-services --puppet-settings syntax. */
    public static @Nullable String editorServicesPuppetSettings(@NotNull PuppetSettingsState.StateData settings) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        if (settings.modulePath != null && !settings.modulePath.isBlank()) {
            values.add("--modulepath");
            values.add(settings.modulePath.trim());
        }
        if (settings.environmentPath != null && !settings.environmentPath.isBlank()) {
            values.add("--environmentpath");
            values.add(settings.environmentPath.trim());
        }
        return values.isEmpty() ? null : String.join(",", values);
    }

    /**
     * Picks a workspace root for Puppet Editor Services. A module root is preferred when possible.
     */
    public static @Nullable Path findWorkspaceRoot(@NotNull Project project, @Nullable VirtualFile file) {
        return findModuleRoot(project, file);
    }
}
