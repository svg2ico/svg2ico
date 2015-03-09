/*
 * Copyright 2015 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico.maven;

import net.sourceforge.writexml.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import static java.util.Arrays.asList;
import static net.sourceforge.writexml.Attributes.attributes;
import static net.sourceforge.writexml.Document.document;
import static net.sourceforge.writexml.Namespace.namespace;
import static net.sourceforge.writexml.Tag.tag;
import static net.sourceforge.writexml.TagName.tagName;
import static net.sourceforge.writexml.Text.text;

public class PomGenerator {

    private static final CompactXmlFormatter XML_FORMATTER = new CompactXmlFormatter();

    public static void main(String[] args) throws Exception {
        final File destination = new File(args[0]);
        final String version = versionString();

        writeXml(pom(version), destination, "pom.xml");
        writeXml(settings(args[1], args[2], args[3], args[4]), destination, "settings.xml");
    }

    private static Tag settings(final String serverId, final String username, final String password, final String gpgPassphrase) {
        return settingsTag(tagName("settings"),
                settingsTag(tagName("servers"),
                        settingsTag(tagName("server"),
                                settingsTag(tagName("id"), text(serverId)),
                                settingsTag(tagName("username"), text(username)),
                                settingsTag(tagName("password"), text(password))
                        )
                ),
                settingsTag(tagName("pluginGroups"),
                        settingsTag(tagName("pluginGroup"), text("org.sonatype.plugins"))
                ),
                settingsTag(tagName("profiles"),
                        settingsTag(tagName("profile"),
                                settingsTag(tagName("id"), text("gpg")),
                                settingsTag(tagName("properties"),
                                        settingsTag(tagName("gpg.passphrase"), text(gpgPassphrase))
                                )
                        )
                )
        );
    }

    private static Tag pom(final String version) {
        return pomTag(tagName("project"),
                pomTag(tagName("modelVersion"), text("4.0.0")),
                pomTag(tagName("groupId"), text("net.sourceforge.svg2ico")),
                pomTag(tagName("artifactId"), text("svg2ico")),
                pomTag(tagName("version"), text(version)),
                pomTag(tagName("packaging"), text("jar")),
                pomTag(tagName("name"), text("svg2ico")),
                pomTag(tagName("description"), text("svg2ico is an open source URI generator and parser written in Java.")),
                pomTag(tagName("url"), text("http://svg2ico.sourceforge.net")),
                pomTag(tagName("licenses"),
                        pomTag(tagName("license"),
                                pomTag(tagName("name"), text("The Apache Software License, Version 2.0")),
                                pomTag(tagName("url"), text("http://www.apache.org/licenses/LICENSE-2.0.txt")),
                                pomTag(tagName("distribution"), text("repo"))
                        )
                ),
                pomTag(tagName("scm"),
                        pomTag(tagName("url"), text("git://git.code.sf.net/p/svg2ico/code"))
                ),
                pomTag(tagName("dependencies")),
                pomTag(tagName("parent"),
                        pomTag(tagName("groupId"), text("org.sonatype.oss")),
                        pomTag(tagName("artifactId"), text("oss-parent")),
                        pomTag(tagName("version"), text("7"))
                )
        );
    }

    private static Tag pomTag(final TagName tagName, final TagContent... children) {
        return tag(namespace(URI.create("http://maven.apache.org/POM/4.0.0")), tagName, attributes(), asList(children));
    }

    private static Tag settingsTag(final TagName tagName, final TagContent... children) {
        return tag(namespace(URI.create("http://maven.apache.org/SETTINGS/1.0.0")), tagName, attributes(), asList(children));
    }

    private static String versionString() throws IOException {
        final Properties properties = new Properties();
        properties.load(new FileReader("version.properties"));
        return properties.getProperty("svg2ico.version.major") + "." + properties.getProperty("svg2ico.version.minor");
    }

    private static void writeXml(final Tag root, final File destination, final String fileName) throws IOException, XmlWriteException {
        final File file = new File(destination, fileName);
        final FileWriter fileWriter = new FileWriter(file);
        XML_FORMATTER.write(document(root), fileWriter);
        fileWriter.close();
    }

}
