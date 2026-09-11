package de.fovea.puppet.runtime;

import com.intellij.execution.configurations.GeneralCommandLine;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record PuppetCommand(@NotNull String executable, @NotNull List<String> prefixArguments) {
    public PuppetCommand {
        prefixArguments = List.copyOf(prefixArguments);
    }

    public PuppetCommand(@NotNull String executable) {
        this(executable, List.of());
    }

    public @NotNull GeneralCommandLine commandLine(@NotNull List<String> arguments, Path workDirectory) {
        GeneralCommandLine command = new GeneralCommandLine();
        command.setExePath(executable);
        List<String> allArguments = new ArrayList<>(prefixArguments);
        allArguments.addAll(arguments);
        command.addParameters(allArguments);
        if (workDirectory != null) command.setWorkDirectory(workDirectory.toFile());
        return command;
    }

    public @NotNull String display() {
        return prefixArguments.isEmpty()
                ? executable
                : executable + " " + String.join(" ", prefixArguments);
    }
}
