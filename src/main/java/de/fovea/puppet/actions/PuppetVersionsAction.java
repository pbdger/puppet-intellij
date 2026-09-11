package de.fovea.puppet.actions;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import de.fovea.puppet.settings.PuppetSettingsState;
import de.fovea.puppet.tools.PuppetToolResolver;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class PuppetVersionsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        List<String> lines = new ArrayList<>();
        lines.add(version("Puppet", PuppetToolResolver.puppet(settings), "--version"));
        lines.add(version("PDK", PuppetToolResolver.pdk(settings), "--version"));
        lines.add(version("puppet-lint", PuppetToolResolver.puppetLint(settings), "--version"));
        lines.add(version("Language server", PuppetToolResolver.languageServer(settings), "--version"));
        Messages.showInfoMessage(project, String.join("\n", lines), "Puppet Tool Versions");
    }

    private static String version(String label, String executable, String argument) {
        if (executable == null) return label + ": not found";
        try {
            ProcessOutput output = new CapturingProcessHandler(new GeneralCommandLine(executable, argument))
                    .runProcess(5000);
            String text = output.getStdout().trim();
            if (text.isEmpty()) text = output.getStderr().trim();
            if (text.isEmpty()) text = "exit " + output.getExitCode();
            return label + ": " + text.replace('\n', ' ');
        } catch (Exception ex) {
            return label + ": error: " + ex.getMessage();
        }
    }
}
