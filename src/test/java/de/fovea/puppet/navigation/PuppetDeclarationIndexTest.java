package de.fovea.puppet.navigation;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import de.fovea.puppet.language.PuppetFileType;

public final class PuppetDeclarationIndexTest extends BasePlatformTestCase {
    public void testFindsQualifiedClassThroughFileBasedIndex() {
        myFixture.addFileToProject("modules/profile/manifests/apache.pp",
                "class profile::apache(String $name = 'x') {}\n");
        myFixture.addFileToProject("modules/profile/manifests/mysql.pp",
                "class profile::mysql {}\n");
        var found = PuppetDeclarationIndex.find(getProject(), "profile::apache");
        assertEquals(1, found.size());
        assertEquals("class", found.get(0).kind());
        assertEquals("profile::apache", found.get(0).name());
    }

    public void testUnknownDeclarationReturnsEmptyList() {
        myFixture.addFileToProject("modules/profile/manifests/apache.pp",
                "class profile::apache {}\n");
        assertTrue(PuppetDeclarationIndex.find(getProject(), "profile::missing").isEmpty());
    }
}
