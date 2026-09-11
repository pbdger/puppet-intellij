package de.fovea.puppet.runtime;

import de.fovea.puppet.settings.PuppetSettingsState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class PuppetRuntimeResolverTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void autoPrefersPdkAndUsesItForEveryTool() throws Exception {
        Path pdkRoot = temporary.newFolder("pdk").toPath();
        executable(pdkRoot, "bin/pdk");
        Path agentRoot = temporary.newFolder("agent").toPath();
        executable(agentRoot, "bin/puppet");
        executable(agentRoot, "bin/ruby");
        Path server = executable(temporary.getRoot().toPath(), "editor-services/puppet-languageserver");

        PuppetRuntime runtime = PuppetRuntimeResolver.resolve(
                new PuppetSettingsState.StateData(), "", List.of(pdkRoot), List.of(agentRoot), server);

        assertNotNull(runtime);
        assertEquals(PuppetRuntimeType.PDK, runtime.type());
        assertEquals(List.of("bundle", "exec", "puppet"), runtime.puppet().prefixArguments());
        assertEquals(List.of("bundle", "exec", "puppet-lint"), runtime.puppetLint().prefixArguments());
        assertEquals(List.of("bundle", "exec", "ruby", server.toString()),
                runtime.languageServer().prefixArguments());
    }

    @Test
    public void autoFallsBackToPuppetAgent() throws Exception {
        Path agentRoot = temporary.newFolder("agent").toPath();
        Path puppet = executable(agentRoot, "bin/puppet");
        Path ruby = executable(agentRoot, "bin/ruby");
        Path server = executable(temporary.getRoot().toPath(), "editor-services/puppet-languageserver");

        PuppetRuntime runtime = PuppetRuntimeResolver.resolve(
                new PuppetSettingsState.StateData(), "", List.of(), List.of(agentRoot), server);

        assertNotNull(runtime);
        assertEquals(PuppetRuntimeType.PUPPET_AGENT, runtime.type());
        assertEquals(puppet.toString(), runtime.puppet().executable());
        assertEquals(ruby.toString(), runtime.ruby().executable());
        assertEquals(List.of(server.toString()), runtime.languageServer().prefixArguments());
        assertNull(runtime.puppetLint());
    }

    @Test
    public void configuredInstallationConstrainsAutoDetection() throws Exception {
        Path selectedAgent = temporary.newFolder("selected-agent").toPath();
        executable(selectedAgent, "bin/puppet");
        executable(selectedAgent, "bin/ruby");
        Path unrelatedPdk = temporary.newFolder("unrelated-pdk").toPath();
        executable(unrelatedPdk, "bin/pdk");
        Path server = executable(temporary.getRoot().toPath(), "editor-services/puppet-languageserver");
        PuppetSettingsState.StateData settings = new PuppetSettingsState.StateData();
        settings.installationDirectory = selectedAgent.toString();

        PuppetRuntime runtime = PuppetRuntimeResolver.resolve(
                settings, "", List.of(unrelatedPdk), List.of(), server);

        assertNotNull(runtime);
        assertEquals(PuppetRuntimeType.PUPPET_AGENT, runtime.type());
        assertEquals(selectedAgent, runtime.installationDirectory());
    }

    @Test
    public void autoKeepsExplicitLegacyLanguageServerAsCustomFallback() throws Exception {
        Path server = executable(temporary.getRoot().toPath(), "legacy/puppet-languageserver");
        PuppetSettingsState.StateData settings = new PuppetSettingsState.StateData();
        settings.languageServerPath = server.toString();

        PuppetRuntime runtime = PuppetRuntimeResolver.resolve(
                settings, "", List.of(), List.of(), server);

        assertNotNull(runtime);
        assertEquals(PuppetRuntimeType.CUSTOM, runtime.type());
        assertEquals(server.toString(), runtime.languageServer().executable());
        assertTrue(runtime.languageServer().prefixArguments().isEmpty());
    }

    private static Path executable(Path root, String relative) throws Exception {
        File file = root.resolve(relative).toFile();
        assertTrue(file.getParentFile().mkdirs() || file.getParentFile().isDirectory());
        assertTrue(file.createNewFile());
        assertTrue(file.setExecutable(true));
        return file.toPath().toAbsolutePath().normalize();
    }
}
