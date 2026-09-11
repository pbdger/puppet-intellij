package de.fovea.puppet.language;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class PuppetLexer extends LexerBase {
    private static final Set<String> KEYWORDS = Set.of(
            "class", "define", "node", "inherits", "if", "elsif", "else", "unless",
            "case", "default", "and", "or", "in", "undef", "true", "false", "include",
            "contain", "require", "realize", "tag", "function", "type", "application",
            "site", "attr", "private"
    );

    private CharSequence buffer = "";
    private int endOffset;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;
        this.tokenStart = startOffset;
        locateToken();
    }

    private void locateToken() {
        if (tokenStart >= endOffset) {
            tokenType = null;
            tokenEnd = tokenStart;
            return;
        }

        char c = buffer.charAt(tokenStart);

        if (Character.isWhitespace(c)) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && Character.isWhitespace(buffer.charAt(tokenEnd))) tokenEnd++;
            tokenType = TokenType.WHITE_SPACE;
            return;
        }

        if (c == '#') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && buffer.charAt(tokenEnd) != '\n') tokenEnd++;
            tokenType = PuppetTokenTypes.COMMENT;
            return;
        }

        if (c == '\'' || c == '"') {
            char quote = c;
            tokenEnd = tokenStart + 1;
            boolean escaped = false;
            while (tokenEnd < endOffset) {
                char ch = buffer.charAt(tokenEnd++);
                if (ch == quote && !escaped) break;
                escaped = ch == '\\' && !escaped;
                if (ch != '\\') escaped = false;
            }
            tokenType = PuppetTokenTypes.STRING;
            return;
        }

        if (c == '$') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset) {
                char ch = buffer.charAt(tokenEnd);
                if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == ':')) break;
                tokenEnd++;
            }
            tokenType = PuppetTokenTypes.VARIABLE;
            return;
        }

        if (Character.isDigit(c)) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset) {
                char ch = buffer.charAt(tokenEnd);
                if (!(Character.isDigit(ch) || ch == '.')) break;
                tokenEnd++;
            }
            tokenType = PuppetTokenTypes.NUMBER;
            return;
        }

        if (Character.isLetter(c) || c == '_') {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset) {
                char ch = buffer.charAt(tokenEnd);
                if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == ':' || ch == '-')) break;
                tokenEnd++;
            }
            String text = buffer.subSequence(tokenStart, tokenEnd).toString();
            tokenType = KEYWORDS.contains(text) ? PuppetTokenTypes.KEYWORD : PuppetTokenTypes.IDENTIFIER;
            return;
        }

        if ("{}[]()".indexOf(c) >= 0) {
            tokenEnd = tokenStart + 1;
            tokenType = PuppetTokenTypes.BRACES;
            return;
        }

        if ("=><!+-*/~|&,.?:;".indexOf(c) >= 0) {
            tokenEnd = tokenStart + 1;
            while (tokenEnd < endOffset && "=><!+-*/~|&".indexOf(buffer.charAt(tokenEnd)) >= 0) tokenEnd++;
            tokenType = PuppetTokenTypes.OPERATOR;
            return;
        }

        tokenEnd = tokenStart + 1;
        tokenType = PuppetTokenTypes.BAD;
    }

    @Override public int getState() { return 0; }
    @Override public @Nullable IElementType getTokenType() { return tokenType; }
    @Override public int getTokenStart() { return tokenStart; }
    @Override public int getTokenEnd() { return tokenEnd; }

    @Override
    public void advance() {
        tokenStart = tokenEnd;
        locateToken();
    }

    @Override public @NotNull CharSequence getBufferSequence() { return buffer; }
    @Override public int getBufferEnd() { return endOffset; }
}
