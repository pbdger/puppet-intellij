package de.fovea.puppet.lsp;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import de.fovea.puppet.project.PuppetProjectUtil;
import de.fovea.puppet.settings.PuppetSettingsState;
import de.fovea.puppet.runtime.PuppetRuntimeResolver;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class PuppetLspServerSupportProvider implements LspServerSupportProvider {
    private static final Logger LOG = Logger.getInstance(PuppetLspServerSupportProvider.class);
    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile file, @NotNull LspServerStarter serverStarter) {
        if (isPuppetFile(file)) {
            serverStarter.ensureServerStarted(new PuppetLspServerDescriptor(project, file));
        }
    }

    public static boolean isPuppetFile(@NotNull VirtualFile file) {
        String name = file.getName();
        String ext = file.getExtension();
        return "Puppetfile".equals(name) || "pp".equalsIgnoreCase(ext) || "epp".equalsIgnoreCase(ext);
    }

    private static final class PuppetLspServerDescriptor extends ProjectWideLspServerDescriptor {
        private final Project project;
        private final VirtualFile contextFile;

        private PuppetLspServerDescriptor(@NotNull Project project, @NotNull VirtualFile contextFile) {
            super(project, "Puppet");
            this.project = project;
            this.contextFile = contextFile;
        }

        @Override
        public boolean isSupportedFile(@NotNull VirtualFile file) {
            return isPuppetFile(file);
        }

        @Override
        public @NotNull GeneralCommandLine createCommandLine() {
            var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
            var runtime = PuppetRuntimeResolver.resolve(settings);
            if (runtime == null || runtime.languageServer() == null) {
                throw new IllegalStateException(
                        "No usable PDK or Puppet Agent runtime was found. Check Settings | Tools | Puppet.");
            }

            Path workspace = PuppetProjectUtil.findWorkspaceRoot(project, contextFile);
            java.util.List<String> arguments = new java.util.ArrayList<>(java.util.List.of("--stdio", "--timeout=0"));
            if (workspace != null) {
                arguments.add("--local-workspace=" + workspace);
            }

            String puppetSettings = PuppetProjectUtil.editorServicesPuppetSettings(settings);
            if (puppetSettings != null) {
                arguments.add("--puppet-settings=" + puppetSettings);
            }
            if (settings.lspDebugLogFile != null && !settings.lspDebugLogFile.isBlank()) {
                arguments.add("--debug=" + settings.lspDebugLogFile.trim());
            }

            GeneralCommandLine command = runtime.languageServer().commandLine(arguments, workspace);

            LOG.info("Starting Puppet language server with runtime: " + runtime.description());
            LOG.info("Puppet language server command: " + runtime.languageServer().display());
            LOG.info("Puppet LSP workspace: " + (workspace == null ? "<none>" : workspace));
            if (puppetSettings != null) LOG.info("Puppet LSP custom Puppet settings enabled");
            if (settings.lspDebugLogFile != null && !settings.lspDebugLogFile.isBlank()) {
                LOG.info("Puppet LSP debug log: " + settings.lspDebugLogFile.trim());
            }
            return command;
        }
    }
}
