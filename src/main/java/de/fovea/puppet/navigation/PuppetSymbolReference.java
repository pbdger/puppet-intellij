package de.fovea.puppet.navigation;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PuppetSymbolReference extends PsiReferenceBase<PsiElement> {
    private final String symbol;

    public PuppetSymbolReference(@NotNull PsiElement element, @NotNull String symbol, int start, int end) {
        super(element, new TextRange(start, end), false);
        this.symbol = symbol;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return PuppetDeclarationIndex.resolveFirst(myElement.getProject(), symbol);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        var file = myElement.getContainingFile();
        var document = PsiDocumentManager.getInstance(myElement.getProject()).getDocument(file);
        if (document != null) {
            int base = myElement.getTextRange().getStartOffset();
            document.replaceString(base + getRangeInElement().getStartOffset(),
                    base + getRangeInElement().getEndOffset(), newElementName);
            PsiDocumentManager.getInstance(myElement.getProject()).commitDocument(document);
        }
        return myElement;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
