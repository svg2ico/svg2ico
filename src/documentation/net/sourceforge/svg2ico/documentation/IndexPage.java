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

import static net.sourceforge.svg2ico.documentation.Svg2IcoPage.anSvg2IcoPage;
import static net.sourceforge.svg2ico.documentation.Svg2IcoPage.jarSvg2Ico;
import static org.sourceforge.xazzle.xhtml.Href.href;
import static org.sourceforge.xazzle.xhtml.Tags.*;

final class IndexPage {

    private IndexPage() {
    }

    public static HtmlTag indexPage(final String version) {
        return anSvg2IcoPage(
                h2Tag(xhtmlText("Introduction")),
                paragraphTag(xhtmlText("svg2ico converts SVG images into ICO images.  It is written in Java, and is available as an Ant task.  It is open source, and free for you to use.")),
                paragraphTag(
                        xhtmlText("The latest version of svg2ico available for download is "),
                        anchorTag(xhtmlText(version)).withHref(href(jarSvg2Ico(version).asString())),
                        xhtmlText(".  The "),
                        anchorTag(xhtmlText("javadoc")).withHref(href("javadoc/")),
                        xhtmlText(" is also available online.")
                )
        );
    }

}
