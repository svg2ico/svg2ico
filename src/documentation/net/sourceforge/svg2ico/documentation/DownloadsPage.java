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

import org.sourceforge.xazzle.xhtml.HtmlTag;

import static net.sourceforge.svg2ico.documentation.Svg2IcoPage.*;
import static net.sourceforge.urin.Authority.authority;
import static net.sourceforge.urin.HierarchicalPart.hierarchicalPart;
import static net.sourceforge.urin.Host.registeredName;
import static net.sourceforge.urin.Path.path;
import static net.sourceforge.urin.Scheme.scheme;
import static net.sourceforge.urin.Urin.urin;
import static net.sourceforge.urin.scheme.Http.http;
import static org.sourceforge.xazzle.xhtml.Href.href;
import static org.sourceforge.xazzle.xhtml.Tags.*;

final class DownloadsPage {

    private DownloadsPage() {
    }

    static HtmlTag downloadsPage(final String version) {
        String standardJarUrl = jarSvg2Ico(version).asString();
        String gitUri = urin(scheme("git"), hierarchicalPart(authority(registeredName("git.code.sf.net")), path("p", "svg2ico", "code"))).asString();
        return anSvg2IcoPage(
                h2Tag(xhtmlText("Downloads")),
                paragraphTag(
                        xhtmlText("svg2ico is available under the "),
                        anchorTag(xhtmlText("Apache 2 license")).withHref(href(http(registeredName("www.apache.org"), path("licenses", "LICENSE-2.0")).asString())),
                        xhtmlText(".  It can be downloaded in three forms:")),
                unorderedListTag(
                        listItemTag(
                                xhtmlText("a "), anchorTag(xhtmlText(" jar")).withHref(href(standardJarUrl)), xhtmlText(", with source code and dependencies included,")
                        ),
                        listItemTag(
                                xhtmlText("as a Maven dependency from central, using "), codeBlock("<dependency>\n" +
                                "   <groupId>net.sourceforge.svg2ico</groupId>\n" +
                                "   <artifactId>svg2ico</artifactId>\n" +
                                "   <version>" + version + "</version>\n" +
                                "</dependency>")
                        ),
                        listItemTag(
                                xhtmlText("or as the full source code including tests etc. using Git from "), codeTag(anchorTag(xhtmlText(gitUri)).withHref(href(gitUri))), xhtmlText(".")
                        )
//                ),
//                paragraphTag(
//                        xhtmlText("It has no runtime dependencies, and all dependencies used in tests are in Git.")
                )
        );
    }

}
