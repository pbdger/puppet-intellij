package de.fovea.puppet.navigation;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import de.fovea.puppet.language.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PuppetFindUsagesProvider implements FindUsagesProvider {
    @Override public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new PuppetLexer(),
                TokenSet.create(PuppetTokenTypes.IDENTIFIER),
                TokenSet.create(PuppetTokenTypes.COMMENT),
                TokenSet.create(PuppetTokenTypes.STRING));
    }
    @Override public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        String t = psiElement.getText();
        return t != null && t.matches("[A-Za-z_][A-Za-z0-9_:]*");
    }
    @Override public @Nullable String getHelpId(@NotNull PsiElement psiElement) { return null; }
    @Override public @NotNull String getType(@NotNull PsiElement element) { return "Puppet symbol"; }
    @Override public @NotNull String getDescriptiveName(@NotNull PsiElement element) { return element.getText(); }
    @Override public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) { return element.getText(); }
}
