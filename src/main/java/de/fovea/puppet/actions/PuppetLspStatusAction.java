package de.fovea.puppet.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.platform.lsp.api.LspServerManager;
import de.fovea.puppet.lsp.PuppetLspServerSupportProvider;
import de.fovea.puppet.settings.PuppetSettingsState;
import de.fovea.puppet.tools.PuppetToolResolver;
import org.jetbrains.annotations.NotNull;

public final class PuppetLspStatusAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        String executable = PuppetToolResolver.languageServer(settings);
        int running = LspServerManager.getInstance(project)
                .getServersForProvider(PuppetLspServerSupportProvider.class).size();
        String message = "Language server: " + (executable == null ? "not found" : executable)
                + "\nRunning Puppet LSP server(s): " + running;
        Messages.showInfoMessage(project, message, "Puppet Language Server");
    }
}
