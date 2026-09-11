package de.consoluta.puppet.lsp;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import de.consoluta.puppet.project.PuppetProjectUtil;
import de.consoluta.puppet.settings.PuppetSettingsState;
import de.consoluta.puppet.tools.PuppetToolResolver;
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
            String server = PuppetToolResolver.languageServer(settings);
            if (server == null) {
                throw new IllegalStateException(
                        "puppet-languageserver was not found. Configure it under Settings | Tools | Puppet.");
            }

            GeneralCommandLine command;
            if (server.toLowerCase().endsWith(".rb")) {
                String ruby = PuppetToolResolver.ruby(settings);
                if (ruby == null) {
                    throw new IllegalStateException(
                            "Ruby was not found, but the configured Puppet language server is a Ruby script.");
                }
                command = new GeneralCommandLine(ruby, server, "--stdio", "--timeout=0");
            } else {
                command = new GeneralCommandLine(server, "--stdio", "--timeout=0");
            }

            Path workspace = PuppetProjectUtil.findWorkspaceRoot(project, contextFile);
            if (workspace != null) {
                command.addParameter("--local-workspace=" + workspace);
                command.setWorkDirectory(workspace.toFile());
            }

            String puppetSettings = PuppetProjectUtil.editorServicesPuppetSettings(settings);
            if (puppetSettings != null) {
                command.addParameter("--puppet-settings=" + puppetSettings);
            }
            if (settings.lspDebugLogFile != null && !settings.lspDebugLogFile.isBlank()) {
                command.addParameter("--debug=" + settings.lspDebugLogFile.trim());
            }

            LOG.info("Starting Puppet language server: " + server);
            LOG.info("Puppet LSP workspace: " + (workspace == null ? "<none>" : workspace));
            if (puppetSettings != null) LOG.info("Puppet LSP custom Puppet settings enabled");
            if (settings.lspDebugLogFile != null && !settings.lspDebugLogFile.isBlank()) {
                LOG.info("Puppet LSP debug log: " + settings.lspDebugLogFile.trim());
            }
            return command;
        }
    }
}
