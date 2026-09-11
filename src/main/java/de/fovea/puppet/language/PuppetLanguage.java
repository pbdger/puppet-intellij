package de.fovea.puppet.language;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public final class PuppetLanguage extends Language {
    public static final PuppetLanguage INSTANCE = new PuppetLanguage();

    private PuppetLanguage() {
        super("Puppet");
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Puppet";
    }
}
