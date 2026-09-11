package de.consoluta.puppet.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.platform.lsp.api.LspServerManager;
import de.consoluta.puppet.lsp.PuppetLspServerSupportProvider;
import org.jetbrains.annotations.NotNull;

public final class PuppetLspRestartAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(PuppetLspServerSupportProvider.class);
    }
}
