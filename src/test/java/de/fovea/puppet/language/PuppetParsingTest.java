package de.fovea.puppet.language;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.psi.PsiFile;

public final class PuppetParsingTest extends BasePlatformTestCase {
    public void testIncompleteManifestStillProducesPuppetPsiFile() {
        PsiFile file = myFixture.configureByText(PuppetFileType.INSTANCE,
                "class profile::apache(\n  String $name,\n) {\n  contain profile::base\n");
        assertInstanceOf(file, PuppetFile.class);
        assertTrue(file.getText().contains("profile::apache"));
        assertNotNull(file.getNode());
    }

    public void testResourceLikeClassSyntaxSurvivesPsi() {
        PsiFile file = myFixture.configureByText(PuppetFileType.INSTANCE,
                "class { 'profile::apache':\n  enabled => true,\n}\n");
        assertEquals("Puppet", file.getLanguage().getID());
        assertTrue(file.getText().contains("'profile::apache'"));
    }
}
