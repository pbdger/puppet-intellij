package de.fovea.puppet.runtime;

import com.intellij.openapi.util.SystemInfo;
import de.fovea.puppet.settings.PuppetSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PuppetRuntimeResolver {
    private PuppetRuntimeResolver() {}

    public static @Nullable PuppetRuntime resolve(@NotNull PuppetSettingsState.StateData settings) {
        return resolve(
                settings,
                System.getenv("PATH"),
                commonPdkRoots(),
                commonAgentRoots(),
                PuppetEditorServices.languageServer());
    }

    static @Nullable PuppetRuntime resolve(
            @NotNull PuppetSettingsState.StateData settings,
            @Nullable String pathEnvironment,
            @NotNull List<Path> pdkRoots,
            @NotNull List<Path> agentRoots,
            @NotNull Path bundledLanguageServer) {
        PuppetRuntimeType requested = PuppetRuntimeType.from(settings.runtimeType);
        Path configuredRoot = path(settings.installationDirectory);

        if (requested == PuppetRuntimeType.AUTO || requested == PuppetRuntimeType.PDK) {
            PuppetRuntime runtime = pdkRuntime(settings, configuredRoot, pathEnvironment, pdkRoots, bundledLanguageServer);
            if (runtime != null) return runtime;
            if (requested == PuppetRuntimeType.PDK) return null;
        }
        if (requested == PuppetRuntimeType.AUTO || requested == PuppetRuntimeType.PUPPET_AGENT) {
            PuppetRuntime runtime = agentRuntime(settings, configuredRoot, pathEnvironment, agentRoots, bundledLanguageServer);
            if (runtime != null) return runtime;
            if (requested == PuppetRuntimeType.PUPPET_AGENT) return null;
        }
        return requested == PuppetRuntimeType.CUSTOM || requested == PuppetRuntimeType.AUTO
                ? customRuntime(settings, pathEnvironment, bundledLanguageServer)
                : null;
    }

    private static @Nullable PuppetRuntime pdkRuntime(
            PuppetSettingsState.StateData settings,
            Path configuredRoot,
            String pathEnvironment,
            List<Path> roots,
            Path bundledLanguageServer) {
        Path pdk = configuredExecutable(settings.pdkExecutable, pathEnvironment);
        if (pdk == null && configuredRoot != null) pdk = executableIn(configuredRoot, "pdk");
        if (pdk == null && configuredRoot == null) pdk = findOnPath("pdk", pathEnvironment);
        if (pdk == null && configuredRoot == null) pdk = findInRoots(roots, "pdk");
        if (pdk == null) return null;

        Path root = configuredRoot != null ? configuredRoot : installationRoot(pdk);
        PuppetCommand pdkCommand = new PuppetCommand(pdk.toString());
        PuppetCommand ruby = new PuppetCommand(pdk.toString(), List.of("bundle", "exec", "ruby"));
        PuppetCommand puppet = overrideOr(settings.puppetExecutable, pathEnvironment,
                new PuppetCommand(pdk.toString(), List.of("bundle", "exec", "puppet")));
        PuppetCommand lint = overrideOr(settings.puppetLintExecutable, pathEnvironment,
                new PuppetCommand(pdk.toString(), List.of("bundle", "exec", "puppet-lint")));
        PuppetCommand server = languageServer(settings, pathEnvironment, ruby, bundledLanguageServer);
        return new PuppetRuntime(PuppetRuntimeType.PDK, root, ruby, puppet, pdkCommand, lint, server);
    }

    private static @Nullable PuppetRuntime agentRuntime(
            PuppetSettingsState.StateData settings,
            Path configuredRoot,
            String pathEnvironment,
            List<Path> roots,
            Path bundledLanguageServer) {
        Path puppet = configuredExecutable(settings.puppetExecutable, pathEnvironment);
        if (puppet == null && configuredRoot != null) puppet = executableIn(configuredRoot, "puppet");
        if (puppet == null && configuredRoot == null) puppet = findOnPath("puppet", pathEnvironment);
        if (puppet == null && configuredRoot == null) puppet = findInRoots(roots, "puppet");
        if (puppet == null) return null;

        Path root = configuredRoot != null ? configuredRoot : installationRoot(puppet);
        Path rubyPath = configuredExecutable(settings.rubyExecutable, pathEnvironment);
        if (rubyPath == null) rubyPath = executableIn(root, "ruby");
        if (rubyPath == null) return null;

        PuppetCommand ruby = new PuppetCommand(rubyPath.toString());
        PuppetCommand lint = configuredCommand(settings.puppetLintExecutable, pathEnvironment);
        if (lint == null) {
            Path bundledLint = executableIn(root, "puppet-lint");
            if (bundledLint != null) lint = new PuppetCommand(bundledLint.toString());
        }
        PuppetCommand pdk = configuredCommand(settings.pdkExecutable, pathEnvironment);
        PuppetCommand server = languageServer(settings, pathEnvironment, ruby, bundledLanguageServer);
        return new PuppetRuntime(PuppetRuntimeType.PUPPET_AGENT, root, ruby,
                new PuppetCommand(puppet.toString()), pdk, lint, server);
    }

    private static @Nullable PuppetRuntime customRuntime(
            PuppetSettingsState.StateData settings,
            String pathEnvironment,
            Path bundledLanguageServer) {
        PuppetCommand ruby = configuredCommand(settings.rubyExecutable, pathEnvironment);
        PuppetCommand puppet = configuredCommand(settings.puppetExecutable, pathEnvironment);
        PuppetCommand pdk = configuredCommand(settings.pdkExecutable, pathEnvironment);
        PuppetCommand lint = configuredCommand(settings.puppetLintExecutable, pathEnvironment);
        PuppetCommand server = languageServer(settings, pathEnvironment, ruby, bundledLanguageServer);
        if (ruby == null && puppet == null && pdk == null && lint == null && server == null) return null;
        return new PuppetRuntime(PuppetRuntimeType.CUSTOM, path(settings.installationDirectory),
                ruby, puppet, pdk, lint, server);
    }

    private static @Nullable PuppetCommand languageServer(
            PuppetSettingsState.StateData settings,
            String pathEnvironment,
            PuppetCommand ruby,
            Path bundledLanguageServer) {
        Path override = configuredExecutable(settings.languageServerPath, pathEnvironment);
        Path server = override == null ? bundledLanguageServer : override;
        if (server == null) return null;
        String lower = server.getFileName().toString().toLowerCase();
        if (!lower.endsWith(".rb") && override != null) return new PuppetCommand(server.toString());
        if (ruby == null) return null;
        List<String> prefix = new ArrayList<>(ruby.prefixArguments());
        prefix.add(server.toString());
        return new PuppetCommand(ruby.executable(), prefix);
    }

    private static @Nullable PuppetCommand overrideOr(String configured, String pathEnvironment, PuppetCommand fallback) {
        PuppetCommand command = configuredCommand(configured, pathEnvironment);
        return command == null ? fallback : command;
    }

    private static @Nullable PuppetCommand configuredCommand(String value, String pathEnvironment) {
        Path executable = configuredExecutable(value, pathEnvironment);
        return executable == null ? null : new PuppetCommand(executable.toString());
    }

    private static @Nullable Path configuredExecutable(String value, String pathEnvironment) {
        if (isAuto(value)) return null;
        String trimmed = value.trim();
        Path candidate = Path.of(trimmed);
        if (candidate.isAbsolute() || trimmed.contains("/") || trimmed.contains("\\")) {
            return usable(candidate) ? candidate.toAbsolutePath().normalize() : null;
        }
        return findOnPath(trimmed, pathEnvironment);
    }

    private static boolean isAuto(String value) {
        return value == null || value.isBlank() || "auto".equalsIgnoreCase(value.trim());
    }

    private static @Nullable Path findInRoots(List<Path> roots, String executable) {
        for (Path root : roots) {
            Path found = executableIn(root, executable);
            if (found != null) return found;
        }
        return null;
    }

    private static @Nullable Path executableIn(Path root, String executable) {
        if (root == null) return null;
        for (String directory : List.of("bin", "private/bin", "private/ruby/bin")) {
            for (String name : executableNames(executable)) {
                Path candidate = root.resolve(directory).resolve(name);
                if (usable(candidate)) return candidate.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    private static @Nullable Path findOnPath(String executable, String pathEnvironment) {
        if (pathEnvironment == null || pathEnvironment.isBlank()) return null;
        for (String directory : pathEnvironment.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            if (directory.isBlank()) continue;
            for (String name : executableNames(executable)) {
                Path candidate = Path.of(directory, name);
                if (usable(candidate)) return candidate.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    private static List<String> executableNames(String name) {
        if (!SystemInfo.isWindows || name.contains(".")) return List.of(name);
        return List.of(name + ".exe", name + ".cmd", name + ".bat", name);
    }

    private static boolean usable(Path path) {
        return Files.isRegularFile(path) && (SystemInfo.isWindows || Files.isExecutable(path));
    }

    private static @Nullable Path path(String value) {
        if (value == null || value.isBlank() || "auto".equalsIgnoreCase(value.trim())) return null;
        return Path.of(value.trim()).toAbsolutePath().normalize();
    }

    private static Path installationRoot(Path executable) {
        Path parent = executable.getParent();
        if (parent == null || parent.getFileName() == null || !"bin".equalsIgnoreCase(parent.getFileName().toString())) {
            return parent;
        }
        Path root = parent.getParent();
        if (root != null) {
            Path agentRoot = root.resolve("puppet");
            if (executableIn(agentRoot, "ruby") != null) return agentRoot;
        }
        return root;
    }

    private static List<Path> commonPdkRoots() {
        List<Path> roots = new ArrayList<>();
        roots.add(Path.of("/opt/puppetlabs/pdk"));
        roots.add(Path.of("/usr/local/opt/pdk"));
        addWindowsRoot(roots, "Puppet Labs", "DevelopmentKit");
        return roots;
    }

    private static List<Path> commonAgentRoots() {
        List<Path> roots = new ArrayList<>();
        roots.add(Path.of("/opt/puppetlabs/puppet"));
        addWindowsRoot(roots, "Puppet Labs", "Puppet");
        return roots;
    }

    private static void addWindowsRoot(List<Path> roots, String... parts) {
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles == null || programFiles.isBlank()) return;
        Path root = Path.of(programFiles);
        for (String part : parts) root = root.resolve(part);
        roots.add(root);
    }
}
