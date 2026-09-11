package de.fovea.puppet.navigation;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import de.fovea.puppet.index.PuppetDeclarationFileIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.*;

public final class PuppetDeclarationIndex {
    private static final Pattern DECL = Pattern.compile(
            "(?m)^\\s*(class|define|function|type)\\s+([A-Za-z_][A-Za-z0-9_:]*)");
    private PuppetDeclarationIndex() {}
    public record Declaration(String kind, String name, PsiFile file, int offset) {}

    public static @NotNull List<Declaration> find(@NotNull Project project, @NotNull String name) {
        List<Declaration> out = new ArrayList<>();
        PsiManager pm = PsiManager.getInstance(project);
        Collection<VirtualFile> candidates = FileBasedIndex.getInstance().getContainingFiles(
                PuppetDeclarationFileIndex.NAME, name, GlobalSearchScope.projectScope(project));
        for (VirtualFile vf : candidates) {
            PsiFile pf = pm.findFile(vf);
            if (pf == null) continue;
            Matcher m = DECL.matcher(pf.getText());
            while (m.find()) {
                if (name.equals(m.group(2))) out.add(new Declaration(m.group(1), name, pf, m.start(2)));
            }
        }
        return out;
    }

    public static @Nullable PsiElement resolveFirst(@NotNull Project project, @NotNull String name) {
        List<Declaration> found = find(project, name);
        if (found.isEmpty()) return null;
        Declaration d = found.get(0);
        return d.file().findElementAt(Math.min(d.offset(), Math.max(0, d.file().getTextLength()-1)));
    }
}
