package de.consoluta.puppet.language;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import junit.framework.TestCase;
import java.util.*;

public final class PuppetLexerTest extends TestCase {
    public void testQualifiedNamesStaySingleTokens() {
        Lexer lexer = new PuppetLexer();
        lexer.start("contain profile::apache");
        List<String> tokens = new ArrayList<>();
        while (lexer.getTokenType() != null) {
            if (lexer.getTokenType() != com.intellij.psi.TokenType.WHITE_SPACE)
                tokens.add(lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString());
            lexer.advance();
        }
        assertEquals(List.of("contain", "profile::apache"), tokens);
    }

    public void testVariablesStringsAndComments() {
        Lexer lexer = new PuppetLexer();
        lexer.start("$foo = 'bar' # comment");
        boolean variable=false, string=false, comment=false;
        while (lexer.getTokenType()!=null) {
            IElementType t=lexer.getTokenType();
            variable |= t == PuppetTokenTypes.VARIABLE;
            string |= t == PuppetTokenTypes.STRING;
            comment |= t == PuppetTokenTypes.COMMENT;
            lexer.advance();
        }
        assertTrue(variable); assertTrue(string); assertTrue(comment);
    }
}
