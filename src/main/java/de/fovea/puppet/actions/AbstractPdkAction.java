package de.fovea.puppet.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import de.fovea.puppet.project.PuppetProjectUtil;
import de.fovea.puppet.settings.PuppetSettingsState;
import de.fovea.puppet.runtime.PuppetRuntimeResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

abstract class AbstractPdkAction extends AnAction {
    protected abstract String[] arguments();
    protected abstract String title();

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        runPdk(project, event.getData(CommonDataKeys.VIRTUAL_FILE), arguments(), title());
    }

    protected final void runPdk(@NotNull Project project, @NotNull String[] args, @NotNull String title) {
        runPdk(project, null, args, title);
    }

    protected final void runPdk(@NotNull Project project,
                                @Nullable VirtualFile contextFile,
                                @NotNull String[] args,
                                @NotNull String title) {
        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        var runtime = PuppetRuntimeResolver.resolve(settings);
        if (runtime == null || runtime.pdk() == null) {
            Messages.showErrorDialog(project,
                    "PDK was not found. Configure it under Settings | Tools | Puppet, or add pdk to PATH.", title);
            return;
        }

        Path workDirectory = PuppetProjectUtil.findModuleRoot(project, contextFile);
        if (workDirectory == null) {
            Messages.showErrorDialog(project, "No local project directory is available.", title);
            return;
        }

        var command = runtime.pdk().commandLine(java.util.List.of(args), workDirectory);

        try {
            OSProcessHandler handler = new OSProcessHandler(command);
            ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            console.attachToProcess(handler);
            console.print("Working directory: " + workDirectory + "\n", com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT);
            console.print("Command: " + command.getCommandLineString() + "\n\n", com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT);

            String id = "Puppet";
            ToolWindowManager manager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = manager.getToolWindow(id);
            if (toolWindow == null) {
                throw new ExecutionException("The Puppet tool window is not registered.");
            }
            toolWindow.getContentManager().removeAllContents(true);
            var content = toolWindow.getContentManager().getFactory().createContent(console.getComponent(), title, false);
            toolWindow.getContentManager().addContent(content);
            toolWindow.show();
            handler.startNotify();
        } catch (ExecutionException ex) {
            Messages.showErrorDialog(project, ex.getMessage() == null ? ex.toString() : ex.getMessage(), title);
        }
    }
}
