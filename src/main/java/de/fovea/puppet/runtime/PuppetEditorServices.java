package de.fovea.puppet.runtime;

import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PuppetEditorServices {
    public static final String VERSION = "2.0.4";
    private static final String RESOURCE =
            "/puppet-editor-services/puppet_editor_services_v" + VERSION + ".zip";

    private PuppetEditorServices() {}

    public static synchronized @NotNull Path languageServer() {
        Path target = Path.of(PathManager.getSystemPath(), "puppet-intellij", "editor-services-" + VERSION);
        Path server = target.resolve("puppet-languageserver");
        Path marker = target.resolve(".complete");
        if (Files.isRegularFile(server) && Files.isRegularFile(marker)) return server;

        try (InputStream resource = PuppetEditorServices.class.getResourceAsStream(RESOURCE)) {
            if (resource == null) {
                throw new IllegalStateException("Bundled Puppet Editor Services " + VERSION + " is missing");
            }
            Files.createDirectories(target);
            extract(resource, target);
            if (!Files.isRegularFile(server)) {
                throw new IllegalStateException("Bundled Puppet Editor Services has no puppet-languageserver entry");
            }
            server.toFile().setExecutable(true, true);
            target.resolve("puppet-languageserver-sidecar").toFile().setExecutable(true, true);
            Files.writeString(marker, VERSION);
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Could not unpack bundled Puppet Editor Services " + VERSION, e);
        }
    }

    private static void extract(InputStream archive, Path target) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(archive)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path output = target.resolve(entry.getName()).normalize();
                if (!output.startsWith(target)) {
                    throw new IOException("Unsafe path in Puppet Editor Services archive: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                } else {
                    Files.createDirectories(output.getParent());
                    Files.copy(zip, output, StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }
}
