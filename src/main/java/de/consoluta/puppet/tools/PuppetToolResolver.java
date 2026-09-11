package de.consoluta.puppet.tools;

import com.intellij.openapi.util.SystemInfo;
import de.consoluta.puppet.settings.PuppetSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PuppetToolResolver {
    private PuppetToolResolver() {}

    public static @Nullable String ruby(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(settings.rubyExecutable, "ruby", List.of(
                "/opt/puppetlabs/pdk/private/ruby/3.2.0/bin/ruby",
                "/opt/puppetlabs/pdk/private/ruby/2.7.0/bin/ruby",
                "/usr/bin/ruby",
                "/usr/local/bin/ruby"
        ));
    }

    public static @Nullable String pdk(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(settings.pdkExecutable, executableName("pdk"), List.of(
                "/opt/puppetlabs/pdk/bin/pdk",
                "/opt/puppetlabs/bin/pdk",
                "/usr/local/bin/pdk",
                "/usr/bin/pdk"
        ));
    }

    public static @Nullable String puppet(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(settings.puppetExecutable, executableName("puppet"), List.of(
                "/opt/puppetlabs/bin/puppet",
                "/usr/local/bin/puppet",
                "/usr/bin/puppet"
        ));
    }

    public static @Nullable String puppetLint(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(settings.puppetLintExecutable, executableName("puppet-lint"), List.of(
                "/opt/puppetlabs/pdk/bin/puppet-lint",
                "/opt/puppetlabs/bin/puppet-lint",
                "/usr/local/bin/puppet-lint",
                "/usr/bin/puppet-lint"
        ));
    }

    public static @Nullable String languageServer(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(settings.languageServerPath, executableName("puppet-languageserver"), List.of(
                "/opt/puppetlabs/pdk/bin/puppet-languageserver",
                "/opt/puppetlabs/bin/puppet-languageserver",
                "/usr/local/bin/puppet-languageserver",
                "/usr/bin/puppet-languageserver"
        ));
    }

    private static String executableName(String base) {
        return SystemInfo.isWindows ? base + ".bat" : base;
    }

    private static @Nullable String resolve(String configured, String commandName, List<String> commonPaths) {
        String value = configured == null ? "" : configured.trim();
        if (!value.isEmpty() && !"auto".equalsIgnoreCase(value)) {
            Path configuredPath = Path.of(value);
            if (configuredPath.isAbsolute() || value.contains("/") || value.contains("\\")) {
                return isUsableExecutable(configuredPath) ? configuredPath.toAbsolutePath().normalize().toString() : null;
            }
            // A bare command name must actually be found; returning an unresolved value only postpones the error.
            return findOnPath(value);
        }

        String found = findOnPath(commandName);
        if (found != null) return found;

        for (String candidate : commonPaths) {
            Path path = Path.of(candidate);
            if (isUsableExecutable(path)) return path.toAbsolutePath().normalize().toString();
        }
        return null;
    }

    private static boolean isUsableExecutable(@NotNull Path path) {
        return Files.isRegularFile(path) && (SystemInfo.isWindows || Files.isExecutable(path));
    }

    private static @Nullable String findOnPath(String executable) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank()) return null;

        List<String> names = new ArrayList<>();
        names.add(executable);
        if (SystemInfo.isWindows && !executable.contains(".")) {
            names.add(executable + ".exe");
            names.add(executable + ".cmd");
            names.add(executable + ".bat");
        }

        for (String directory : pathEnv.split(java.io.File.pathSeparator)) {
            if (directory.isBlank()) continue;
            for (String name : names) {
                Path candidate = Path.of(directory, name);
                if (isUsableExecutable(candidate)) {
                    return candidate.toAbsolutePath().normalize().toString();
                }
            }
        }
        return null;
    }
}
