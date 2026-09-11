package de.fovea.puppet.language;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.*;
import org.jetbrains.annotations.NotNull;

public final class PuppetParserDefinition implements ParserDefinition {
    @Override public @NotNull Lexer createLexer(Project project) { return new PuppetLexer(); }
    @Override public @NotNull PsiParser createParser(Project project) { return new PuppetParser(); }
    @Override public @NotNull IFileElementType getFileNodeType() {
        return Holder.FILE;
    }
    private static final class Holder {
        static final IFileElementType FILE = new IFileElementType(PuppetLanguage.INSTANCE);
    }
    @Override public @NotNull TokenSet getWhitespaceTokens() { return TokenSet.create(TokenType.WHITE_SPACE); }
    @Override public @NotNull TokenSet getCommentTokens() { return TokenSet.create(PuppetTokenTypes.COMMENT); }
    @Override public @NotNull TokenSet getStringLiteralElements() { return TokenSet.create(PuppetTokenTypes.STRING); }
    @Override public @NotNull PsiElement createElement(ASTNode node) { return new ASTWrapperPsiElement(node); }
    @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) { return new PuppetFile(viewProvider); }
}
