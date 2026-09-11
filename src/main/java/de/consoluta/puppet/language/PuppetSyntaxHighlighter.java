package de.consoluta.puppet.language;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public final class PuppetSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("PUPPET_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    private static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("PUPPET_STRING", DefaultLanguageHighlighterColors.STRING);
    private static final TextAttributesKey COMMENT = TextAttributesKey.createTextAttributesKey("PUPPET_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    private static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("PUPPET_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    private static final TextAttributesKey VARIABLE = TextAttributesKey.createTextAttributesKey("PUPPET_VARIABLE", DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    private static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey("PUPPET_BRACES", DefaultLanguageHighlighterColors.BRACES);
    private static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey("PUPPET_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    private static final TextAttributesKey BAD = TextAttributesKey.createTextAttributesKey("PUPPET_BAD", HighlighterColors.BAD_CHARACTER);
    private static final TextAttributesKey[] EMPTY = TextAttributesKey.EMPTY_ARRAY;

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new PuppetLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType == PuppetTokenTypes.KEYWORD) return pack(KEYWORD);
        if (tokenType == PuppetTokenTypes.STRING) return pack(STRING);
        if (tokenType == PuppetTokenTypes.COMMENT) return pack(COMMENT);
        if (tokenType == PuppetTokenTypes.NUMBER) return pack(NUMBER);
        if (tokenType == PuppetTokenTypes.VARIABLE) return pack(VARIABLE);
        if (tokenType == PuppetTokenTypes.BRACES) return pack(BRACES);
        if (tokenType == PuppetTokenTypes.OPERATOR) return pack(OPERATOR);
        if (tokenType == PuppetTokenTypes.BAD) return pack(BAD);
        return EMPTY;
    }
}
