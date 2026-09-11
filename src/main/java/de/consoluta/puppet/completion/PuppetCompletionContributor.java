package de.consoluta.puppet.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import de.consoluta.puppet.index.PuppetDeclarationFileIndex;
import de.consoluta.puppet.language.PuppetLanguage;
import org.jetbrains.annotations.NotNull;

public final class PuppetCompletionContributor extends CompletionContributor {
    public PuppetCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(PuppetLanguage.INSTANCE),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement pos = parameters.getPosition();
                        String prefix = pos.getContainingFile().getText()
                                .substring(Math.max(0, pos.getTextOffset() - 80), pos.getTextOffset());
                        if (!prefix.matches("(?s).*(?:include|contain|require|realize)\\s+[A-Za-z0-9_:]*$"))
                            return;
                        FileBasedIndex.getInstance().processAllKeys(
                                PuppetDeclarationFileIndex.NAME,
                                name -> {
                                    result.addElement(LookupElementBuilder.create(name).withTypeText("Puppet symbol", true));
                                    return true;
                                },
                                parameters.getPosition().getProject());
                    }
                });
    }
}
