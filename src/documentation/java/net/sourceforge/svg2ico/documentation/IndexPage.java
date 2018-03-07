/*
 * Copyright 2018 Mark Slater
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.sourceforge.svg2ico.documentation;

import net.sourceforge.xazzle.xhtml.HtmlTag;

import static net.sourceforge.svg2ico.documentation.Svg2IcoPage.*;
import static net.sourceforge.xazzle.xhtml.Href.href;
import static net.sourceforge.xazzle.xhtml.Tags.*;

final class IndexPage {

    private IndexPage() {
    }

    public static HtmlTag indexPage(final String version) {
        final String svg2IcoPath = "lib/build/svg2ico-" + version + ".jar";
        return anSvg2IcoPage(
                h2Tag(xhtmlText("Introduction")),
                paragraphTag(xhtmlText("svg2ico converts SVG images into ICO images.  It is written in Java, and is available as an Ant task.  It is open source, and free for you to use.")),
                paragraphTag(
                        xhtmlText("The latest version of svg2ico available for download is "),
                        anchorTag(xhtmlText(version)).withHref(href(jarSvg2Ico(version).asString())),
                        xhtmlText(".  The "),
                        anchorTag(xhtmlText("javadoc")).withHref(href("javadoc/")),
                        xhtmlText(" is also available online.")
                ),
                h2Tag(xhtmlText("Command Line Example")),
                paragraphTag(xhtmlText("svg2ico can be used from the command line to convert "), codeSnippet("resources/favicon.svg"),
                        xhtmlText(" to an ICO like this:")),
                codeBlock("./svg2ico-" + version + ".jar -src resources/favicon.svg -dest favicon.ico -width 32 -height 32"),
                paragraphTag(xhtmlText("Three additional optional arguments are supported.  "), codeSnippet("depth"),
                        xhtmlText(" specifies the colour depth in bits per pixel, e.g. "), codeSnippet("-depth 8"),
                        xhtmlText(" outputs eight bits per pixel.  The "), codeSnippet("-compress"), xhtmlText(" flag causes the output to be compressed ICO format.  The "),
                        codeSnippet("-userStylesheet"), xhtmlText(" allows a user stylesheet file to use during rendering to be provided, for example "), codeSnippet("-userStylesheet ./my-style.css"), xhtmlText(".")),
                h2Tag(xhtmlText("Ant Example")),
                paragraphTag(xhtmlText("svg2ico can be used as an Ant task to convert "), codeSnippet("resources/favicon.svg"),
                        xhtmlText(" to an ICO like this:")),
                codeBlock("<target name=\"Convert SVG to ICO\">\n" +
                        "    <taskdef name=\"svg2ico\"\n" +
                        "             classname=\"net.sourceforge.svg2ico.Svg2IcoTask\" \n" +
                        "             classpath=\"" + svg2IcoPath + "\"/>\n" +
                        "    <svg2ico src=\"resources/favicon.svg\"\n" +
                        "             dest=\"resources/favicon.ico\"\n" +
                        "             width=\"32\"\n" +
                        "             height=\"32\"/>\n" +
                        "</target>"),
                paragraphTag(xhtmlText("where "), codeSnippet(svg2IcoPath), xhtmlText(" points to where the svg2ico jar " +
                        "can be found.  As with the command line, three optional attributes are supported.  "), codeSnippet("depth"),
                        xhtmlText(" specifies the colour depth in bits per pixel, e.g. "), codeSnippet("depth=\"8\""), xhtmlText(" outputs eight bits per pixel.  "),
                        codeSnippet("compress"), xhtmlText(" causes the output to be compressed ICO, e.g. "), codeSnippet("compressed=\"true\""), xhtmlText(".  "),
                        codeSnippet("userStylesheet"), xhtmlText(" specifies a user stylesheet to use for rendering, e.g. "), codeSnippet("userStylesheet=\"resources/favicon.css\""), xhtmlText(".")),
                h2Tag(xhtmlText("Credits")),
                paragraphTag(xhtmlText("svg2ico uses the excellent "), anchorTag(xhtmlText("Batik")).withHref(href("http://xmlgraphics.apache.org/batik/")),
                        xhtmlText(" and "), anchorTag(xhtmlText("image4j")).withHref(href("http://image4j.sourceforge.net/")),
                        xhtmlText(", but you don't need to worry about these dependencies because they're included in the distribution " +
                                "using the equally excellent "), anchorTag(xhtmlText("Jar Jar Links")).withHref(href("http://code.google.com/p/jarjar/")),
                        xhtmlText("."))
        );
    }

}
