package de.fovea.puppet.language;

import com.intellij.lang.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Tolerant first-stage Puppet parser. It preserves every lexer token in the AST.
 * Semantic declaration/reference indexing is handled separately, so incomplete
 * manifests remain navigable while editing.
 */
public final class PuppetParser implements PsiParser {
    public static final IElementType FILE = new IElementType("PUPPET_FILE", PuppetLanguage.INSTANCE);

    @Override
    public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        PsiBuilder.Marker file = builder.mark();
        while (!builder.eof()) builder.advanceLexer();
        file.done(root);
        return builder.getTreeBuilt();
    }
}
