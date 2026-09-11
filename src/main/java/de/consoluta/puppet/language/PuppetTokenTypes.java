package de.consoluta.puppet.language;

import com.intellij.psi.tree.IElementType;

public final class PuppetTokenTypes {
    private PuppetTokenTypes() {}

    public static final IElementType COMMENT = new IElementType("PUPPET_COMMENT", PuppetLanguage.INSTANCE);
    public static final IElementType STRING = new IElementType("PUPPET_STRING", PuppetLanguage.INSTANCE);
    public static final IElementType NUMBER = new IElementType("PUPPET_NUMBER", PuppetLanguage.INSTANCE);
    public static final IElementType KEYWORD = new IElementType("PUPPET_KEYWORD", PuppetLanguage.INSTANCE);
    public static final IElementType VARIABLE = new IElementType("PUPPET_VARIABLE", PuppetLanguage.INSTANCE);
    public static final IElementType IDENTIFIER = new IElementType("PUPPET_IDENTIFIER", PuppetLanguage.INSTANCE);
    public static final IElementType BRACES = new IElementType("PUPPET_BRACES", PuppetLanguage.INSTANCE);
    public static final IElementType OPERATOR = new IElementType("PUPPET_OPERATOR", PuppetLanguage.INSTANCE);
    public static final IElementType BAD = new IElementType("PUPPET_BAD", PuppetLanguage.INSTANCE);
}
