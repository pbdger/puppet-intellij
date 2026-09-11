package de.consoluta.puppet.language;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public final class PuppetFile extends PsiFileBase {
    public PuppetFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PuppetLanguage.INSTANCE);
    }
    @Override public @NotNull PuppetFileType getFileType() { return PuppetFileType.INSTANCE; }
    @Override public String toString() { return "Puppet File"; }
}
