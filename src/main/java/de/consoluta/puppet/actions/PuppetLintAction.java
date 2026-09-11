package de.consoluta.puppet.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import de.consoluta.puppet.project.PuppetProjectUtil;
import de.consoluta.puppet.settings.PuppetSettingsState;
import de.consoluta.puppet.tools.PuppetToolResolver;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public final class PuppetLintAction extends AbstractPuppetToolAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null || !isManifest(file)) return;

        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        String lint = PuppetToolResolver.puppetLint(settings);
        if (lint == null) {
            Messages.showErrorDialog(project,
                    "puppet-lint was not found. Configure it under Settings | Tools | Puppet, or add puppet-lint to PATH.",
                    "Puppet Lint");
            return;
        }

        Path workDir = PuppetProjectUtil.findModuleRoot(project, file);
        if (workDir == null) return;
        runCommand(project, lint, List.of(file.getPath()), workDir, "Puppet Lint");
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = event.getProject() != null && file != null && isManifest(file);
        event.getPresentation().setEnabledAndVisible(visible);
    }

    private static boolean isManifest(@NotNull VirtualFile file) {
        return !file.isDirectory() && "pp".equalsIgnoreCase(file.getExtension());
    }
}
