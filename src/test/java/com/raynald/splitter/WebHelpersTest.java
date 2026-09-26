package com.raynald.splitter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WebHelpersTest {

    @Test
    void parsesFormDataIncludingRepeatedAndEncodedFields() {
        Map<String, List<String>> form =
                WebServer.parseForm("name=Alice+Tan&sharedBy=Bob&sharedBy=Charlie%21");

        assertEquals(List.of("Alice Tan"), form.get("name"));
        assertEquals(List.of("Bob", "Charlie!"), form.get("sharedBy"));
    }

    @Test
    void escapesHtmlSoUserTextCantBecomeCode() {
        assertEquals("&lt;b&gt;Tom &amp; Jerry&lt;/b&gt;", HtmlPage.escape("<b>Tom & Jerry</b>"));
    }
}