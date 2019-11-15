/*
 * Copyright 2019 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico.documentation;

import net.sourceforge.writexml.CompactXmlFormatter;
import net.sourceforge.writexml.XmlWriteException;
import net.sourceforge.xazzle.xhtml.HtmlTag;

import java.io.*;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.sourceforge.svg2ico.documentation.DownloadsPage.downloadsPage;
import static net.sourceforge.svg2ico.documentation.IndexPage.indexPage;
import static net.sourceforge.svg2ico.documentation.SupportPage.supportPage;

public class DocumentationGenerator {

    private static final CompactXmlFormatter XML_FORMATTER = new CompactXmlFormatter();

    public static void main(String[] args) throws Exception {
        final File destination = new File(args[0]);
        final String version = versionString();

        writePage(indexPage(version), destination, "index.html");
        writePage(downloadsPage(version), destination, "downloads.html");
        writePage(supportPage(), destination, "support.html");
    }

    private static String versionString() throws IOException {
        Properties properties = new Properties();
        try (
                final FileInputStream fileInputStream = new FileInputStream("gradle.properties");
                final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, UTF_8)
        ) {
            properties.load(inputStreamReader);
            return properties.getProperty("majorVersion") + "." + properties.getProperty("minorVersion");
        }
    }

    private static void writePage(final HtmlTag svg2IcoPage, final File destination, final String fileName) throws IOException, XmlWriteException {
        try (
                final FileOutputStream fileOutputStream = new FileOutputStream(new File(destination, fileName));
                final OutputStreamWriter fileWriter = new OutputStreamWriter(fileOutputStream, UTF_8)
        ) {
            XML_FORMATTER.write(svg2IcoPage.asDocument(), fileWriter);
        }
    }

}
