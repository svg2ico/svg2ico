/*
 * Copyright 2012 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico.documentation;

import org.sourceforge.writexml.Document;
import org.sourceforge.writexml.JaxpXmlWriter;
import org.sourceforge.writexml.XmlWriterException;
import org.sourceforge.xazzle.xhtml.HtmlTag;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static net.sourceforge.svg2ico.documentation.DownloadsPage.downloadsPage;
import static net.sourceforge.svg2ico.documentation.IndexPage.indexPage;
import static net.sourceforge.svg2ico.documentation.SupportPage.supportPage;

public class DocumentationGenerator {
    public static void main(String[] args) throws Exception {
        final File destination = new File(args[0]);
        final String version = versionString();

        writePage(indexPage(version), destination, "index.html");
        writePage(downloadsPage(version), destination, "downloads.html");
        writePage(supportPage(), destination, "support.html");
    }

    private static String versionString() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("version.properties"));
        return properties.getProperty("svg2ico.version.major") + "." + properties.getProperty("svg2ico.version.minor");
    }

    private static void writePage(final HtmlTag indexPage, final File destination, final String fileName) throws IOException, XmlWriterException {
        final File file = new File(destination, fileName);
        final FileWriter fileWriter = new FileWriter(file);
        new JaxpXmlWriter(fileWriter).write(new Document(indexPage.asWriteableToXml()));
        fileWriter.close();
    }

}
