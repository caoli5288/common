package com.mengcraft.util;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MavenLibs {

    private static final String LOCAL_REPOSITORY = System.getProperty("user.home") + "/.m2/repository";
    private static final String CENTRAL = "https://mirrors.huaweicloud.com/repository/maven";

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    private static Method INVOKER_addURL;

    static {
        try {
            INVOKER_addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            INVOKER_addURL.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    private final String ns;
    private final String basename;
    private final boolean optional;
    private final String scope;

    private MavenLibs(String groupId, String artifactId, String version, boolean optional, String scope) {
        basename = artifactId + "-" + version;
        ns = groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        this.optional = optional;
        this.scope = scope;
    }

    public void load() {
        load(MavenLibs.class.getClassLoader());
    }

    @SneakyThrows
    public void load(ClassLoader cl) {
        Preconditions.checkState(cl instanceof URLClassLoader, "Current classloader not instanceof URLClassLoader");
        if (!isNullOrEquals(scope, "compile") || optional) {
            return;
        }
        // process depends first
        List<MavenLibs> depends = depends();
        if (!depends.isEmpty()) {
            for (MavenLibs depend : depends) {
                depend.load(cl);
            }
        }
        // hack into classloader
        Logger logger = Bukkit.getLogger();
        logger.info(String.format("Load MavenLibs(%s)", ns));
        File jar = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".jar");
        if (!jar.exists()) {
            String url = CENTRAL + "/" + ns + "/" + basename + ".jar";
            logger.info("Get " + url);
            downloads(jar, url);
        }
        INVOKER_addURL.invoke(cl, jar.toURI().toURL());
    }

    @SneakyThrows
    private List<MavenLibs> depends() {
        File pom = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".pom");
        if (!pom.exists()) {
            downloads(pom, CENTRAL + "/" + ns + "/" + basename + ".pom");
        }
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(pom);
        XPath x = X_PATH_FACTORY.newXPath();
        // result container
        List<MavenLibs> result = new ArrayList<>();
        // parent
        Node parent = (Node) x.evaluate("/project/parent", doc, XPathConstants.NODE);
        if (parent != null) {
            result.add(of(mapOf(parent)));
        }
        // dependencies
        NodeList dependencies = (NodeList) x.evaluate("/project/dependencies/dependency", doc, XPathConstants.NODESET);
        int length = dependencies.getLength();
        for (int i = 0; i < length; i++) {
            result.add(of(mapOf(dependencies.item(i))));
        }
        return result;
    }

    @SneakyThrows
    private void downloads(File f, String url) {
        File parent = f.getParentFile();
        Preconditions.checkState(parent.exists() || parent.mkdirs(), "mkdirs");
        File tmp = File.createTempFile("MavenLibs", ".jar");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            try (FileOutputStream fs = new FileOutputStream(tmp)) {
                ByteStreams.copy(connection.getInputStream(), fs);
            }
            if (f.exists()) {// async downloads?
                Preconditions.checkState(tmp.delete(), "delete tmp");
            } else {
                Files.move(tmp, f);
            }
        } catch (Exception e) {
            Preconditions.checkState(tmp.delete(), "delete tmp");
        } finally {
            connection.disconnect();
        }
    }

    private static Map<String, String> mapOf(Node node) {
        Map<String, String> result = new HashMap<>();
        NodeList childNodes = node.getChildNodes();
        int length = childNodes.getLength();
        for (int i = 0; i < length; i++) {
            Node _node = childNodes.item(i);
            if (_node.hasChildNodes()) {
                result.put(_node.getNodeName(), _node.getFirstChild().getNodeValue());
            }
        }
        return result;
    }

    private static boolean isNullOrEquals(String obj, String comp) {
        return obj == null || obj.equals(comp);
    }

    public static MavenLibs of(String groupId, String artifactId, String version) {
        return new MavenLibs(groupId, artifactId, version, false, null);
    }

    public static MavenLibs of(Map<String, String> map) {
        return new MavenLibs(map.get("groupId"), map.get("artifactId"), map.get("version"), Boolean.parseBoolean(map.get("optional")), map.get("scope"));
    }
}
