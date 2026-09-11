package de.fovea.puppet.navigation;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public final class PuppetReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement().withLanguage(de.fovea.puppet.language.PuppetLanguage.INSTANCE),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(
                            @NotNull PsiElement element, @NotNull ProcessingContext context) {
                        PsiFile file = element.getContainingFile();
                        if (file == null || !"pp".equalsIgnoreCase(file.getVirtualFile() == null ? "" : file.getVirtualFile().getExtension()))
                            return PsiReference.EMPTY_ARRAY;

                        String raw = element.getText();
                        if (raw == null || raw.isBlank()) return PsiReference.EMPTY_ARRAY;
                        String token = raw;
                        int rangeStart = 0;
                        int rangeEnd = raw.length();
                        if ((raw.startsWith("'") && raw.endsWith("'")) || (raw.startsWith("\"") && raw.endsWith("\""))) {
                            token = raw.substring(1, raw.length() - 1);
                            rangeStart = 1;
                            rangeEnd = raw.length() - 1;
                        }
                        if (!token.matches("[A-Za-z_][A-Za-z0-9_:]*"))
                            return PsiReference.EMPTY_ARRAY;

                        String text = file.getText();
                        int start = element.getTextRange().getStartOffset();
                        int left = Math.max(0, start - 80);
                        String prefix = text.substring(left, start);

                        // Reference-bearing Puppet forms. Resource-like class declarations are
                        // intentionally included; arbitrary identifiers are not.
                        if (!prefix.matches("(?s).*(?:include|contain|require|realize)\\s+$")
                                && !prefix.matches("(?s).*class\\s*\\{\\s*['\"]?$")) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        return new PsiReference[]{ new PuppetSymbolReference(element, token, rangeStart, rangeEnd) };
                    }
                });
    }
}
