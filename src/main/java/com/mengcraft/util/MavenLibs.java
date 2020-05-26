package com.mengcraft.util;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavenLibs {

    private static final String LOCAL_REPOSITORY = System.getProperty("user.home") + "/.m2/repository/";
    private static final String CENTRAL = "https://repo1.maven.org/maven2/";

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

    private final String groupId;
    private final String artifactId;
    private final String version;

    private String ns;
    private String basename;
    private File pom;
    private File jar;

    private MavenLibs(Map<String, String> map) {
        this(map.get("groupId"), map.get("artifactId"), map.get("version"));
    }

    public MavenLibs(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        prepare();
    }

    private void prepare() {
        basename = artifactId + "-" + version;
        ns = groupId.replace('.', '/') + "/" + artifactId + "/" + version;
        pom = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".pom");
        jar = new File(LOCAL_REPOSITORY, ns + "/" + basename + ".jar");
    }

    @SneakyThrows
    public void apply(Plugin plugin) {
        plugin.getLogger().info(String.format("Get MavenLibs(groupId=%s, artifactId=%s, version=%s)", groupId, artifactId, version));
        List<MavenLibs> depends = depends();
        if (!depends.isEmpty()) {
            for (MavenLibs depend : depends) {
                depend.apply(plugin);
            }
        }
        if (!jar.exists()) {
            downloads(pom, CENTRAL + ns + "/" + basename + ".jar");
        }
        INVOKER_addURL.invoke(plugin.getClass().getClassLoader(), jar.toURI().toURL());
    }

    @SneakyThrows
    private List<MavenLibs> depends() {
        if (!pom.exists()) {
            downloads(pom, CENTRAL + ns + "/" + basename + ".pom");
        }
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(pom);
        XPath x = X_PATH_FACTORY.newXPath();
        // result container
        List<MavenLibs> result = new ArrayList<>();
        // parent
        Node parent = (Node) x.evaluate("/project/parent", doc, XPathConstants.NODE);
        if (parent != null) {
            result.add(new MavenLibs(mapOf(parent)));
        }
        // dependencies
        NodeList dependencies = (NodeList) x.evaluate("/project/dependencies/dependency[not(scope) or scope=\"compile\"]", doc, XPathConstants.NODESET);
        int length = dependencies.getLength();
        for (int i = 0; i < length; i++) {
            result.add(new MavenLibs(mapOf(dependencies.item(i))));
        }
        return result;
    }

    @SneakyThrows
    private void downloads(File f, String url) {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            try (FileOutputStream fs = new FileOutputStream(f)) {
                ByteStreams.copy(connection.getInputStream(), fs);
            }
        } catch (Exception ignored) {
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
}
