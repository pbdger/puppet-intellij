package de.consoluta.puppet.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.*;

public final class PuppetClassNavigationAction extends AnAction {
    private static final Pattern DECL = Pattern.compile("(?m)^\\s*(?:class|define)\\s+([A-Za-z_][A-Za-z0-9_:]*)");

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        String requested = Messages.showInputDialog(project,
                "Enter Puppet class or defined type (for example profile::apache):",
                "Go to Puppet Class", null);
        if (requested == null || requested.isBlank()) return;

        List<PuppetDeclarationIndex.Declaration> declarations = PuppetDeclarationIndex.find(project, requested.trim());
        List<PsiFile> matches = declarations.stream().map(PuppetDeclarationIndex.Declaration::file).distinct().toList();
        if (matches.isEmpty()) {
            Messages.showInfoMessage(project, "No Puppet class or defined type named '" + requested + "' was found.", "Puppet");
            return;
        }
        matches.get(0).navigate(true);
    }

    private static List<PsiFile> find(Project project, String name) {
        List<PsiFile> result = new ArrayList<>();
        PsiManager pm = PsiManager.getInstance(project);
        Collection<VirtualFile> files = FileTypeIndex.getFiles(
                de.consoluta.puppet.language.PuppetFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));
        for (VirtualFile vf : files) {
            if (!"pp".equalsIgnoreCase(vf.getExtension())) continue;
            PsiFile pf = pm.findFile(vf);
            if (pf == null) continue;
            Matcher m = DECL.matcher(pf.getText());
            while (m.find()) {
                if (name.equals(m.group(1))) {
                    result.add(pf);
                    break;
                }
            }
        }
        return result;
    }
}
