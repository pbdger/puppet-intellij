package de.consoluta.puppet.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;

public final class PuppetRenameProcessor extends RenamePsiElementProcessor {
    @Override public boolean canProcessElement(@NotNull PsiElement element) {
        if (element.getLanguage() != de.consoluta.puppet.language.PuppetLanguage.INSTANCE) return false;
        String t = element.getText();
        if (t == null || !t.matches("[A-Za-z_][A-Za-z0-9_:]*")) return false;
        for (PsiReference reference : element.getReferences()) {
            if (reference instanceof PuppetSymbolReference) return true;
        }
        return !PuppetDeclarationIndex.find(element.getProject(), t).isEmpty();
    }

    @Override
    public void renameElement(@NotNull PsiElement element, @NotNull String newName,
                              UsageInfo @NotNull [] usages, RefactoringElementListener listener) {
        // Leaf tokens from the tolerant PSI do not implement PsiNamedElement yet.
        // Rename references first; declaration replacement is done via the document.
        for (UsageInfo usage : usages) {
            PsiReference ref = usage.getReference();
            if (ref != null) ref.handleElementRename(newName);
        }
        PsiFile file = element.getContainingFile();
        if (file != null) {
            var document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
            if (document != null) {
                var r = element.getTextRange();
                document.replaceString(r.getStartOffset(), r.getEndOffset(), newName);
                PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
            }
        }
        if (listener != null) listener.elementRenamed(element);
    }
}
