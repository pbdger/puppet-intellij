package de.fovea.puppet.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.platform.lsp.api.LspServerManager;
import de.fovea.puppet.lsp.PuppetLspServerSupportProvider;
import de.fovea.puppet.settings.PuppetSettingsState;
import de.fovea.puppet.runtime.PuppetRuntimeResolver;
import org.jetbrains.annotations.NotNull;

public final class PuppetLspStatusAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        var runtime = PuppetRuntimeResolver.resolve(settings);
        int running = LspServerManager.getInstance(project)
                .getServersForProvider(PuppetLspServerSupportProvider.class).size();
        String message = "Runtime: " + (runtime == null ? "not found" : runtime.description())
                + "\nLanguage server: " + (runtime == null || runtime.languageServer() == null
                    ? "not found" : runtime.languageServer().display())
                + "\nRunning Puppet LSP server(s): " + running;
        Messages.showInfoMessage(project, message, "Puppet Language Server");
    }
}
