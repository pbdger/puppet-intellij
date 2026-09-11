package de.fovea.puppet.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class PuppetFileType extends LanguageFileType {
    public static final PuppetFileType INSTANCE = new PuppetFileType();

    private PuppetFileType() {
        super(PuppetLanguage.INSTANCE);
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Puppet";
    }

    @Override
    public @Nls @NotNull String getDescription() {
        return "Puppet manifest or template";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "pp";
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
