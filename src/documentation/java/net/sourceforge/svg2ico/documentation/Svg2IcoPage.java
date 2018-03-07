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

import net.sourceforge.urin.Host;
import net.sourceforge.urin.Path;
import net.sourceforge.urin.Urin;
import net.sourceforge.xazzle.xhtml.*;

import static net.sourceforge.urin.Authority.authority;
import static net.sourceforge.urin.Host.registeredName;
import static net.sourceforge.urin.Path.path;
import static net.sourceforge.urin.scheme.http.Http.HTTP;
import static net.sourceforge.urin.scheme.http.HttpQuery.queryParameter;
import static net.sourceforge.urin.scheme.http.HttpQuery.queryParameters;
import static net.sourceforge.urin.scheme.http.Https.HTTPS;
import static net.sourceforge.xazzle.xhtml.AlternateText.alternateText;
import static net.sourceforge.xazzle.xhtml.ClassName.className;
import static net.sourceforge.xazzle.xhtml.Href.href;
import static net.sourceforge.xazzle.xhtml.Id.id;
import static net.sourceforge.xazzle.xhtml.ImageSource.imageSource;
import static net.sourceforge.xazzle.xhtml.MetaContent.metaContent;
import static net.sourceforge.xazzle.xhtml.MetaName.metaName;
import static net.sourceforge.xazzle.xhtml.MimeType.mimeType;
import static net.sourceforge.xazzle.xhtml.Relationship.STYLESHEET;
import static net.sourceforge.xazzle.xhtml.Relationship.relationship;
import static net.sourceforge.xazzle.xhtml.Tags.*;
import static net.sourceforge.xazzle.xhtml.XhtmlDimension.pixels;

final class Svg2IcoPage {
    private static final Host SOURCEFORGE = registeredName("sourceforge.net");
    private static final Host W3_JIGSAW = registeredName("jigsaw.w3.org");
    private static final Host W3_WWW = registeredName("www.w3.org");

    public static HtmlTag anSvg2IcoPage(final BlockElement<DoesNotContainFormTag>... body) {
        final Href projectSiteHref = href(HTTP.urin(authority(SOURCEFORGE), path("projects", "svg2ico")).asString());
        return htmlTag(
                headTag(
                        titleTag("svg2ico - A Java library for converting images in SVG format to ICO format"),
                        linkTag()
                                .withRelationships(STYLESHEET)
                                .withMimeType(mimeType("text/css"))
                                .withHref(href("svg2ico.css")),
                        linkTag()
                                .withRelationships(relationship("icon"))
                                .withMimeType(mimeType("image/png"))
                                .withHref(href("favicon-32x32.png")),
                        metaTag(metaName("description"), metaContent("svg2ico is a Java library for converting SVG images to ICO images.  It is free to download and use in your project.")),
                        scriptTag(mimeType("text/javascript"), xhtmlText("  var _gaq = _gaq || [];\n" +
                                "  _gaq.push(['_setAccount', 'UA-16431822-7']);\n" +
                                "  _gaq.push(['_trackPageview']);\n" +
                                "\n" +
                                "  (function() {\n" +
                                "    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n" +
                                "    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n" +
                                "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n" +
                                "  })();"))
                ),
                bodyTag(
                        divTag(
                                divTag(
                                        Tags.h1Tag(Tags.xhtmlText("svg2ico"))
                                ).withId(Id.id("header")),
                                divTag(
                                        unorderedListTag(
                                                listItemTag(
                                                        Tags.anchorTag(Tags.xhtmlText("Home"))
                                                                .withHref(href(HTTP.relativeReference(Path.rootlessPath("index.html")).asString()))
                                                ),
                                                listItemTag(
                                                        Tags.anchorTag(Tags.xhtmlText("Downloads"))
                                                                .withHref(href(HTTP.relativeReference(Path.rootlessPath("downloads.html")).asString()))
                                                ),
                                                listItemTag(
                                                        Tags.anchorTag(Tags.xhtmlText("Project Site"))
                                                                .withHref(projectSiteHref)
                                                )
                                        )
                                ).withId(id("navigation")),
                                divTag(body).withId(id("content")),
                                divTag(
                                        unorderedListTag(
                                                listItemTag(
                                                        anchorTag(
                                                                imageTag(
                                                                        imageSource(HTTP.urin(authority(registeredName("sflogo.sourceforge.net")), path("sflogo.php"), queryParameters(queryParameter("group_id", "740443"), queryParameter("type", "13"))).asString()),
                                                                        alternateText("Get svg2ico at SourceForge.net. Fast, secure and Free Open Source software downloads")
                                                                )
                                                                        .withHeight(pixels("30"))
                                                                        .withWidth(pixels("120"))
                                                        ).withHref(projectSiteHref)
                                                ),
                                                listItemTag(
                                                        anchorTag(
                                                                imageTag(
                                                                        imageSource(HTTP.urin(authority(W3_JIGSAW), path("css-validator", "images", "vcss")).asString()),
                                                                        alternateText("Valid CSS!")
                                                                )
                                                                        .withHeight(pixels("31"))
                                                                        .withWidth(pixels("88"))
                                                        ).withHref(href(HTTP.urin(authority(W3_JIGSAW), path("css-validator", "check", "referer")).asString()))
                                                ),
                                                listItemTag(
                                                        anchorTag(
                                                                imageTag(
                                                                        imageSource(HTTP.urin(authority(W3_WWW), path("Icons", "valid-xhtml10")).asString()),
                                                                        alternateText("Valid XHTML 1.0 Strict")
                                                                )
                                                                        .withHeight(pixels("31"))
                                                                        .withWidth(pixels("88"))
                                                        ).withHref(href(HTTP.urin(authority(registeredName("validator.w3.org")), path("check"), queryParameters(queryParameter("uri", "referer"))).asString()))
                                                )
                                        )
                                ).withId(id("footer"))
                        ).withId(id("root"))
                )
        );
    }

    static InlineTag codeSnippet(String snippet) {
        return codeTag(xhtmlText(snippet));
    }

    static BlockElement<DoesNotContainFormTag> codeBlock(String someCode) {
        return divTag(
                xhtmlText(someCode)
        ).withClass(className("code"));
    }

    static Urin jarSvg2Ico(final String version) {
        return HTTPS.urin(authority(registeredName("sourceforge.net")), path("projects", "svg2ico", "files", version, "svg2ico-" + version + ".jar", "download"));
    }
}
