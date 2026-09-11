package de.consoluta.puppet.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

abstract class AbstractPuppetToolAction extends AnAction {
    protected final void runCommand(@NotNull Project project,
                                    @NotNull String executable,
                                    @NotNull List<String> arguments,
                                    @NotNull Path workDirectory,
                                    @NotNull String title) {
        var command = new GeneralCommandLine();
        command.setExePath(executable);
        command.addParameters(arguments);
        command.setWorkDirectory(workDirectory.toFile());

        try {
            OSProcessHandler handler = new OSProcessHandler(command);
            ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            console.attachToProcess(handler);
            console.print("Working directory: " + workDirectory + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
            console.print("Command: " + command.getCommandLineString() + "\n\n", ConsoleViewContentType.SYSTEM_OUTPUT);

            ToolWindowManager manager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = manager.getToolWindow("Puppet");
            if (toolWindow == null) {
                toolWindow = manager.registerToolWindow("Puppet", true, ToolWindowAnchor.BOTTOM);
            }
            toolWindow.getContentManager().removeAllContents(true);
            var content = toolWindow.getContentManager().getFactory()
                    .createContent(console.getComponent(), title, false);
            toolWindow.getContentManager().addContent(content);
            toolWindow.show();
            handler.startNotify();
        } catch (ExecutionException ex) {
            Messages.showErrorDialog(project,
                    ex.getMessage() == null ? ex.toString() : ex.getMessage(), title);
        }
    }
}
