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
import net.sourceforge.xazzle.xhtml.HtmlTag;

import static net.sourceforge.svg2ico.documentation.Svg2IcoPage.anSvg2IcoPage;
import static net.sourceforge.urin.Authority.authority;
import static net.sourceforge.urin.Host.registeredName;
import static net.sourceforge.urin.Path.path;
import static net.sourceforge.urin.scheme.http.Https.HTTPS;
import static net.sourceforge.xazzle.xhtml.Href.href;
import static net.sourceforge.xazzle.xhtml.Tags.*;

final class SupportPage {

    private static final Host SOURCE_FORGE = registeredName("sourceforge.net");

    private SupportPage() {
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
    static HtmlTag supportPage() {
        return anSvg2IcoPage(
                h2Tag(xhtmlText("Support")),
                paragraphTag(
                        xhtmlText("The best way to get help on svg2ico is via the "),
                        anchorTag(xhtmlText("help forum")).withHref(href(HTTPS.urin(authority(SOURCE_FORGE), path("p", "svg2ico", "discussion", "help")).asString())),
                        xhtmlText(".")),
                paragraphTag(
                        xhtmlText("Alternatively, report a bug or request a feature by "),
                        anchorTag(xhtmlText("raising a ticket")).withHref(href(HTTPS.urin(authority(SOURCE_FORGE), path("p", "svg2ico", "tickets")).asString())),
                        xhtmlText(".")
                ),
                paragraphTag(
                        xhtmlText("Finally, there is an "),
                        anchorTag(xhtmlText("open discussion forum")).withHref(href(HTTPS.urin(authority(SOURCE_FORGE), path("p", "svg2ico", "discussion", "general")).asString())),
                        xhtmlText(" for anything else.")
                )
        );
    }
}
