package de.fovea.puppet.structure;

import com.intellij.ide.structureView.*;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.regex.*;

public final class PuppetStructureViewFactory implements PsiStructureViewFactory {
    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @Override
            public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return new PuppetStructureModel(psiFile, editor);
            }
        };
    }

    private static final class PuppetStructureModel extends StructureViewModelBase
            implements StructureViewModel.ElementInfoProvider {
        PuppetStructureModel(PsiFile file, Editor editor) {
            super(file, editor, new RootElement(file));
            withSorters(Sorter.ALPHA_SORTER);
        }
        @Override public boolean isAlwaysShowsPlus(StructureViewTreeElement element) { return false; }
        @Override public boolean isAlwaysLeaf(StructureViewTreeElement element) {
            return element instanceof SymbolElement;
        }
    }

    private static final class RootElement implements StructureViewTreeElement {
        private final PsiFile file;
        RootElement(PsiFile file) { this.file = file; }
        @Override public Object getValue() { return file; }
        @Override public void navigate(boolean requestFocus) { file.navigate(requestFocus); }
        @Override public boolean canNavigate() { return file.canNavigate(); }
        @Override public boolean canNavigateToSource() { return file.canNavigateToSource(); }
        @Override public @NotNull ItemPresentation getPresentation() { return file.getPresentation(); }

        @Override
        public StructureViewTreeElement @NotNull [] getChildren() {
            List<StructureViewTreeElement> result = new ArrayList<>();
            String text = file.getText();
            Pattern p = Pattern.compile("(?m)^\\s*(class|define|function|type|node)\\s+([A-Za-z_][A-Za-z0-9_:.-]*)\\s*(\\([^)]*\\))?");
            Matcher m = p.matcher(text);
            while (m.find()) {
                String kind = m.group(1);
                String name = m.group(2);
                String params = m.group(3);
                int offset = m.start(2);
                result.add(new SymbolElement(file, kind, name, params, offset));
            }
            return result.toArray(StructureViewTreeElement[]::new);
        }
    }

    private static final class SymbolElement implements StructureViewTreeElement {
        private final PsiFile file;
        private final String kind, name, params;
        private final int offset;
        SymbolElement(PsiFile file, String kind, String name, String params, int offset) {
            this.file=file; this.kind=kind; this.name=name; this.params=params; this.offset=offset;
        }
        @Override public Object getValue() { return file.findElementAt(Math.min(offset, Math.max(0, file.getTextLength()-1))); }
        @Override public void navigate(boolean requestFocus) {
            PsiElement e = file.findElementAt(offset);
            if (e instanceof Navigatable navigatable && navigatable.canNavigate()) {
                navigatable.navigate(requestFocus);
            }
        }
        @Override public boolean canNavigate() {
            PsiElement e = file.findElementAt(offset);
            return e instanceof Navigatable navigatable && navigatable.canNavigate();
        }
        @Override public boolean canNavigateToSource() {
            PsiElement e = file.findElementAt(offset);
            return e instanceof Navigatable navigatable && navigatable.canNavigateToSource();
        }
        @Override public StructureViewTreeElement @NotNull [] getChildren() { return new StructureViewTreeElement[0]; }
        @Override public @NotNull ItemPresentation getPresentation() {
            return new ItemPresentation() {
                @Override public String getPresentableText() {
                    if (params == null || params.isBlank()) return name;
                    String compact = params.replaceAll("\\s+", " ");
                    return name + " " + (compact.length() > 70 ? compact.substring(0,67) + "..." : compact);
                }
                @Override public @Nullable String getLocationString() { return kind; }
                @Override public @Nullable Icon getIcon(boolean unused) { return null; }
            };
        }
    }
}
