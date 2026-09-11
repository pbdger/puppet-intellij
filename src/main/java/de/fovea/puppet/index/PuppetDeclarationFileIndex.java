package de.fovea.puppet.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.*;

public final class PuppetDeclarationFileIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> NAME = ID.create("puppet.declaration.names.v1");
    private static final Pattern DECL = Pattern.compile(
            "(?m)^\\s*(?:class|define|function|type)\\s+([A-Za-z_][A-Za-z0-9_:]*)");

    private final DataIndexer<String, Void, FileContent> indexer = input -> {
        Map<String, Void> out = new HashMap<>();
        Matcher m = DECL.matcher(input.getContentAsText());
        while (m.find()) out.put(m.group(1), null);
        return out;
    };

    @Override public @NotNull ID<String, Void> getName() { return NAME; }
    @Override public @NotNull DataIndexer<String, Void, FileContent> getIndexer() { return indexer; }
    @Override public @NotNull KeyDescriptor<String> getKeyDescriptor() { return EnumeratorStringDescriptor.INSTANCE; }
    @Override public @NotNull DataExternalizer<Void> getValueExternalizer() {
        return new DataExternalizer<>() {
            @Override public void save(@NotNull java.io.DataOutput out, Void value) {}
            @Override public Void read(@NotNull java.io.DataInput in) { return null; }
        };
    }
    @Override public int getVersion() { return 1; }
    @Override public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return file -> "pp".equalsIgnoreCase(file.getExtension());
    }
    @Override public boolean dependsOnFileContent() { return true; }
}
