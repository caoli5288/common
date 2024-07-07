package com.mengcraft.util;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.logging.Logger;

public class Artifact {

    // constants
    private static final String MAVEN_HOME = System.getProperty("maven.home", System.getProperty("user.home") + "/.m2");
    private static final String MAVEN_LOCAL_REPO = MAVEN_HOME + "/repository";
    private static final String MAVEN_REPOSITORY = System.getProperty("maven.repository", "https://maven.aliyun.com/repository/public");
    private static final String ARTIFACT_PATH = "/%GROUP_PATH%/%ARTIFACT%/%VERSION%/%ARTIFACT%-%VERSION%.jar";
    // utils
    private static final Logger LOGGER = Bukkit.getLogger();
    // stats
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final File file;

    Artifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        file = new File(MAVEN_LOCAL_REPO + ARTIFACT_PATH
                .replace("%GROUP_PATH%", groupId.replace('.', '/'))
                .replace("%ARTIFACT%", artifactId)
                .replace("%VERSION%", version));
    }

    public static Artifact of(String groupId, String artifactId, String version) {
        return new Artifact(groupId, artifactId, version);
    }

    public static Artifact of(String descriptor) {
        String[] desc = descriptor.split(":");
        return new Artifact(desc[0], desc[1], desc[2]);
    }

    public static void load(JavaPlugin plugin, List<String> artifacts) throws IOException {
        load(plugin.getClass().getClassLoader(), artifacts);
    }

    public static void load(ClassLoader cl, List<String> artifacts) throws IOException {
        URLClassLoader ucl = (URLClassLoader) cl;
        for (String artifact : artifacts) {
            of(artifact).load(ucl);
        }
    }

    public void load(URLClassLoader cl) throws IOException {
        LOGGER.info("load " + groupId + ":" + artifactId + ":" + version);
        load0();
        URLClassLoaderAccessor.addUrl(cl, file.toURI().toURL());
    }

    public File getFile() {
        return file;
    }

    void load0() throws IOException {
        // return if file exists
        if (file.exists()) {
            return;
        }
        LOGGER.info("load " + file);
        // ensure parents
        Files.createParentDirs(file);
        // temp file
        File tmp = File.createTempFile("tmp", ".tmp", file.getParentFile());
        tmp.deleteOnExit();
        // download from maven repository
        URL url = new URL(MAVEN_REPOSITORY + ARTIFACT_PATH
                .replace("%GROUP_PATH%", groupId.replace('.', '/'))
                .replace("%ARTIFACT%", artifactId)
                .replace("%VERSION%", version));
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        try {
            httpConn.addRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Java; x64)");
            httpConn.connect();
            try (InputStream from = httpConn.getInputStream()) {
                try (FileOutputStream to = new FileOutputStream(tmp)) {
                    ByteStreams.copy(from, to);
                }
            }
            Files.move(tmp, file);
        } finally {
            httpConn.disconnect();
        }
    }
}
