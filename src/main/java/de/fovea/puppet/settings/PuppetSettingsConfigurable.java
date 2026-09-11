package de.fovea.puppet.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import de.fovea.puppet.runtime.PuppetRuntime;
import de.fovea.puppet.runtime.PuppetRuntimeResolver;
import de.fovea.puppet.runtime.PuppetRuntimeType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class PuppetSettingsConfigurable implements Configurable {
    private JComboBox<PuppetRuntimeType> runtimeTypeField;
    private JBTextField installationDirectoryField;
    private JBTextField rubyField;
    private JBTextField serverField;
    private JBTextField pdkField;
    private JBTextField puppetField;
    private JBTextField lintField;
    private JBTextField modulePathField;
    private JBTextField environmentPathField;
    private JBTextField lspDebugLogField;

    private PuppetSettingsState settings() {
        return ApplicationManager.getApplication().getService(PuppetSettingsState.class);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() { return "Puppet"; }

    @Override
    public @Nullable JComponent createComponent() {
        var data = settings().data();
        runtimeTypeField = new JComboBox<>(PuppetRuntimeType.values());
        runtimeTypeField.setSelectedItem(PuppetRuntimeType.from(data.runtimeType));
        installationDirectoryField = new JBTextField(data.installationDirectory);
        rubyField = new JBTextField(data.rubyExecutable);
        serverField = new JBTextField(data.languageServerPath);
        pdkField = new JBTextField(data.pdkExecutable);
        puppetField = new JBTextField(data.puppetExecutable);
        lintField = new JBTextField(data.puppetLintExecutable);
        modulePathField = new JBTextField(data.modulePath);
        environmentPathField = new JBTextField(data.environmentPath);
        lspDebugLogField = new JBTextField(data.lspDebugLogFile);
        JButton detectButton = new JButton("Detect tools");
        detectButton.addActionListener(e -> detectTools());

        return FormBuilder.createFormBuilder()
                .addComponent(new JBLabel("Puppet runtime"))
                .addLabeledComponent(new JBLabel("Runtime:"), runtimeTypeField, 1, false)
                .addLabeledComponent(new JBLabel("Installation directory:"), installationDirectoryField, 1, false)
                .addComponent(new JBLabel("AUTO prefers PDK, then Puppet Agent. Puppet Editor Services is bundled with the plugin."))
                .addComponent(detectButton)
                .addSeparator()
                .addComponent(new JBLabel("Advanced executable overrides (use 'auto' for the selected runtime)"))
                .addLabeledComponent(new JBLabel("Ruby executable:"), rubyField, 1, false)
                .addLabeledComponent(new JBLabel("Language server override:"), serverField, 1, false)
                .addLabeledComponent(new JBLabel("PDK executable:"), pdkField, 1, false)
                .addLabeledComponent(new JBLabel("Puppet executable:"), puppetField, 1, false)
                .addLabeledComponent(new JBLabel("puppet-lint executable:"), lintField, 1, false)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Module path:"), modulePathField, 1, false)
                .addLabeledComponent(new JBLabel("Environment path:"), environmentPathField, 1, false)
                .addComponent(new JBLabel("Paths are optional. Use the platform path separator (: on Unix, ; on Windows)."))
                .addSeparator()
                .addLabeledComponent(new JBLabel("LSP debug log file:"), lspDebugLogField, 1, false)
                .addComponent(new JBLabel("Leave empty to disable LSP debug logging. Debug output is written to a file, never STDOUT."))
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void detectTools() {
        var probe = snapshot();
        List<String> lines = new ArrayList<>();
        PuppetRuntime runtime = PuppetRuntimeResolver.resolve(probe);
        if (runtime == null) {
            lines.add("No usable PDK or Puppet Agent runtime was found.");
        } else {
            lines.add("Runtime: " + runtime.description());
            lines.add("Ruby: " + display(runtime.ruby()));
            lines.add("Language server: " + display(runtime.languageServer()));
            lines.add("PDK: " + display(runtime.pdk()));
            lines.add("Puppet: " + display(runtime.puppet()));
            lines.add("puppet-lint: " + display(runtime.puppetLint()));
        }
        Messages.showInfoMessage(String.join("\n", lines), "Puppet Tool Detection");
    }

    private PuppetSettingsState.StateData snapshot() {
        var probe = new PuppetSettingsState.StateData();
        probe.runtimeType = ((PuppetRuntimeType) runtimeTypeField.getSelectedItem()).name();
        probe.installationDirectory = installationDirectoryField.getText().trim();
        probe.rubyExecutable = rubyField.getText().trim();
        probe.languageServerPath = serverField.getText().trim();
        probe.pdkExecutable = pdkField.getText().trim();
        probe.puppetExecutable = puppetField.getText().trim();
        probe.puppetLintExecutable = lintField.getText().trim();
        probe.modulePath = modulePathField.getText().trim();
        probe.environmentPath = environmentPathField.getText().trim();
        probe.lspDebugLogFile = lspDebugLogField.getText().trim();
        return probe;
    }

    private static String display(de.fovea.puppet.runtime.PuppetCommand value) {
        return value == null ? "not found" : value.display();
    }

    @Override
    public boolean isModified() {
        var data = settings().data();
        var form = snapshot();
        return !form.runtimeType.equals(data.runtimeType)
                || !form.installationDirectory.equals(data.installationDirectory)
                || !form.rubyExecutable.equals(data.rubyExecutable)
                || !form.languageServerPath.equals(data.languageServerPath)
                || !form.pdkExecutable.equals(data.pdkExecutable)
                || !form.puppetExecutable.equals(data.puppetExecutable)
                || !form.puppetLintExecutable.equals(data.puppetLintExecutable)
                || !form.modulePath.equals(data.modulePath)
                || !form.environmentPath.equals(data.environmentPath)
                || !form.lspDebugLogFile.equals(data.lspDebugLogFile);
    }

    @Override
    public void apply() {
        var data = settings().data();
        var form = snapshot();
        data.runtimeType = form.runtimeType;
        data.installationDirectory = form.installationDirectory;
        data.rubyExecutable = form.rubyExecutable;
        data.languageServerPath = form.languageServerPath;
        data.pdkExecutable = form.pdkExecutable;
        data.puppetExecutable = form.puppetExecutable;
        data.puppetLintExecutable = form.puppetLintExecutable;
        data.modulePath = form.modulePath;
        data.environmentPath = form.environmentPath;
        data.lspDebugLogFile = form.lspDebugLogFile;
    }
}
