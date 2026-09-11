package de.consoluta.puppet.navigation;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class PuppetModuleInfoAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null) return;
        VirtualFile dir = file != null && !file.isDirectory() ? file.getParent() : file;
        if (dir == null && project.getBaseDir() != null) dir = project.getBaseDir();
        VirtualFile module = findModuleRoot(dir);
        if (module == null) {
            Messages.showInfoMessage(project, "No parent directory containing metadata.json was found.", "Puppet Module");
            return;
        }
        List<String> parts = new ArrayList<>();
        for (String n : List.of("manifests","templates","files","lib","spec","tasks","plans","facts.d")) {
            if (module.findChild(n) != null) parts.add(n + "/");
        }
        Messages.showInfoMessage(project,
                "Module root: " + module.getPath() + "\nDetected areas: " +
                        (parts.isEmpty() ? "(none)" : String.join(", ", parts)),
                "Puppet Module");
    }

    private static VirtualFile findModuleRoot(VirtualFile dir) {
        for (VirtualFile cur=dir; cur!=null; cur=cur.getParent()) {
            if (cur.findChild("metadata.json") != null) return cur;
        }
        return null;
    }
}
