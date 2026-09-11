package de.consoluta.puppet.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import de.consoluta.puppet.tools.PuppetToolResolver;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public final class PuppetSettingsConfigurable implements Configurable {
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
                .addComponent(new JBLabel("Use 'auto' to search PATH and common Puppet Labs installation directories."))
                .addLabeledComponent(new JBLabel("Ruby executable:"), rubyField, 1, false)
                .addLabeledComponent(new JBLabel("Puppet language server:"), serverField, 1, false)
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
                .addComponent(detectButton)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void detectTools() {
        var probe = snapshot();
        List<String> lines = new ArrayList<>();
        lines.add("Ruby: " + display(PuppetToolResolver.ruby(probe)));
        lines.add("Language server: " + display(PuppetToolResolver.languageServer(probe)));
        lines.add("PDK: " + display(PuppetToolResolver.pdk(probe)));
        lines.add("Puppet: " + display(PuppetToolResolver.puppet(probe)));
        lines.add("puppet-lint: " + display(PuppetToolResolver.puppetLint(probe)));
        Messages.showInfoMessage(String.join("\n", lines), "Puppet Tool Detection");
    }

    private PuppetSettingsState.StateData snapshot() {
        var probe = new PuppetSettingsState.StateData();
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

    private static String display(String value) { return value == null ? "not found" : value; }

    @Override
    public boolean isModified() {
        var data = settings().data();
        var form = snapshot();
        return !form.rubyExecutable.equals(data.rubyExecutable)
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
