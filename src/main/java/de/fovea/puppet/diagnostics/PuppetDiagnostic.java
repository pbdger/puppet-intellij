package de.fovea.puppet.diagnostics;

import com.intellij.lang.annotation.HighlightSeverity;
import org.jetbrains.annotations.NotNull;

public record PuppetDiagnostic(int line, int column, @NotNull HighlightSeverity severity, @NotNull String message) {}
