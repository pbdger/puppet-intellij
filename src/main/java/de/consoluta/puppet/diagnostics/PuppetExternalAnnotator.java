package de.consoluta.puppet.diagnostics;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import de.consoluta.puppet.project.PuppetProjectUtil;
import de.consoluta.puppet.settings.PuppetSettingsState;
import de.consoluta.puppet.tools.PuppetToolResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Runs puppet parser validate and puppet-lint in the background and maps their diagnostics into editor annotations. */
public final class PuppetExternalAnnotator extends ExternalAnnotator<PuppetExternalAnnotator.Input, List<PuppetDiagnostic>> {
    private static final Pattern LINT = Pattern.compile("^(.*?):(\\d+):(\\d+):\\s*(.*)$");
    private static final Pattern PUPPET_LINE = Pattern.compile("(?i).*?line\\s+(\\d+)(?:[:,]\\s*column\\s+(\\d+))?.*");

    public record Input(@NotNull PsiFile psiFile, @NotNull VirtualFile file, @NotNull Path workDir) {}

    @Override
    public @Nullable Input collectInformation(@NotNull PsiFile file) {
        VirtualFile vf = file.getVirtualFile();
        if (vf == null || vf.isDirectory() || !"pp".equalsIgnoreCase(vf.getExtension())) return null;
        Path root = PuppetProjectUtil.findModuleRoot(file.getProject(), vf);
        return root == null ? null : new Input(file, vf, root);
    }

    @Override
    public @Nullable List<PuppetDiagnostic> doAnnotate(Input input) {
        var settings = ApplicationManager.getApplication().getService(PuppetSettingsState.class).data();
        List<PuppetDiagnostic> result = new ArrayList<>();

        String puppet = PuppetToolResolver.puppet(settings);
        if (puppet != null) {
            List<String> args = new ArrayList<>(List.of("parser", "validate"));
            PuppetProjectUtil.addPuppetPathArguments(args, settings);
            args.add(input.file().getPath());
            ProcessOutput out = run(puppet, args, input.workDir());
            if (out != null && out.getExitCode() != 0) parsePuppet(out.getStderr() + "\n" + out.getStdout(), result);
        }

        String lint = PuppetToolResolver.puppetLint(settings);
        if (lint != null) {
            ProcessOutput out = run(lint, List.of("--no-autoloader_layout-check", input.file().getPath()), input.workDir());
            if (out != null) parseLint(out.getStdout() + "\n" + out.getStderr(), result);
        }
        return result;
    }

    private static @Nullable ProcessOutput run(String executable, List<String> args, Path workDir) {
        try {
            GeneralCommandLine command = new GeneralCommandLine();
            command.setExePath(executable);
            command.addParameters(args);
            command.setWorkDirectory(workDir.toFile());
            return new CapturingProcessHandler(command).runProcess(15_000);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void parseLint(String text, List<PuppetDiagnostic> result) {
        for (String line : text.split("\\R")) {
            Matcher m = LINT.matcher(line.trim());
            if (!m.matches()) continue;
            String message = m.group(4).trim();
            HighlightSeverity severity = message.matches("^[EW]:.*") && message.startsWith("E:")
                    ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
            message = message.replaceFirst("^[EWC]:\\s*", "");
            result.add(new PuppetDiagnostic(parse(m.group(2), 1), parse(m.group(3), 1), severity, "puppet-lint: " + message));
        }
    }

    private static void parsePuppet(String text, List<PuppetDiagnostic> result) {
        for (String raw : text.split("\\R")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            Matcher m = PUPPET_LINE.matcher(line);
            int ln = 1, col = 1;
            if (m.matches()) {
                ln = parse(m.group(1), 1);
                col = parse(m.group(2), 1);
            }
            String message = line.replaceFirst("(?i)^error:\\s*", "");
            result.add(new PuppetDiagnostic(ln, col, HighlightSeverity.ERROR, "Puppet: " + message));
            break; // parser errors commonly repeat the same root cause
        }
    }

    private static int parse(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); } catch (NumberFormatException e) { return fallback; }
    }

    @Override
    public void apply(@NotNull PsiFile file, List<PuppetDiagnostic> diagnostics, @NotNull AnnotationHolder holder) {
        Document document = file.getViewProvider().getDocument();
        if (document == null || diagnostics == null) return;
        for (PuppetDiagnostic d : diagnostics) {
            int line = Math.max(0, Math.min(document.getLineCount() - 1, d.line() - 1));
            int start = document.getLineStartOffset(line);
            int end = document.getLineEndOffset(line);
            int offset = Math.min(end, start + Math.max(0, d.column() - 1));
            TextRange range = offset < end ? new TextRange(offset, offset + 1) : new TextRange(start, Math.max(start + 1, end));
            holder.newAnnotation(d.severity(), d.message()).range(range).create();
        }
    }
}
