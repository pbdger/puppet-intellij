package de.consoluta.puppet.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public final class PdkNewDefinedTypeAction extends AbstractPdkAction {
    @Override protected String[] arguments() { return new String[0]; }
    @Override protected String title() { return "PDK New Defined Type"; }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        String name = Messages.showInputDialog(project, "Defined type name:", title(), Messages.getQuestionIcon());
        if (name == null || name.isBlank()) return;
        runPdk(project, event.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE), new String[]{"new", "defined_type", name.trim()}, title());
    }
}
