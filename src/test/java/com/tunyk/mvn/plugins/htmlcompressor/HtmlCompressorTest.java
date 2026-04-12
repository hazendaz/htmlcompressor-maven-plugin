/*
 *    Copyright 2011-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.tunyk.mvn.plugins.htmlcompressor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HtmlCompressorTest.
 */
class HtmlCompressorTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HtmlCompressorTest.class);

    /**
     * Sets the up class.
     */
    @BeforeAll
    static void setUpClass() {
        LOG.info("Setting up class...");
    }

    /**
     * Tear down class.
     */
    @AfterAll
    static void tearDownClass() {
        LOG.info("Test finished.");
    }

    /**
     * Sets the up.
     */
    @BeforeEach
    void setUp() {
        LOG.info("Setting up data for testing...");
    }

    /**
     * Test compress with default settings produces output files.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompress() throws Exception {
        LOG.info("Testing compress method...");

        HtmlCompressor htmlCompressor = new HtmlCompressor("src/test/resources/html", "target/test/htmlcompressor/0");
        htmlCompressor.compress();

        // Verify output files were created
        Assertions.assertTrue(Files.exists(Path.of("target/test/htmlcompressor/0/templates/Template1.html")),
                "Template1.html should be created in output");
        Assertions.assertTrue(Files.exists(Path.of("target/test/htmlcompressor/0/templates/Template2.html")),
                "Template2.html should be created in output");
        Assertions.assertTrue(Files.exists(Path.of("target/test/htmlcompressor/0/templates/recursive/Template.html")),
                "Recursive Template.html should be created in output");

        // Verify content is compressed (comments removed by default)
        String compressed = Files.readString(Path.of("target/test/htmlcompressor/0/templates/Template1.html"));
        Assertions.assertFalse(compressed.contains("<!--"), "Compressed output should not contain HTML comments");
        Assertions.assertTrue(compressed.contains("template #1"),
                "Compressed output should still contain main content");

        htmlCompressor = new HtmlCompressor("src/test/resources/html", "target/test/htmlcompressor/1", true,
                "target/test/htmlcompressor/1/integration.js", "src/test/resources/html/integration.js");
        htmlCompressor.compress();

        // Verify JSON sprite file was created
        Assertions.assertTrue(Files.exists(Path.of("target/test/htmlcompressor/1/integration.js")),
                "JSON integration file should be created");
        String jsonContent = Files.readString(Path.of("target/test/htmlcompressor/1/integration.js"));
        Assertions.assertTrue(jsonContent.contains("template"), "Integration JS should contain template references");

        htmlCompressor = new HtmlCompressor("src/test/resources/html", "target/test/htmlcompressor/2");
        com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressorHandler = new com.googlecode.htmlcompressor.compressor.HtmlCompressor();
        htmlCompressorHandler.setEnabled(false);
        htmlCompressor.setHtmlCompressor(htmlCompressorHandler);
        htmlCompressor.compress();

        // When compressor is disabled, output should match input (content not compressed)
        String disabledOutput = Files.readString(Path.of("target/test/htmlcompressor/2/templates/Template1.html"));
        Assertions.assertTrue(disabledOutput.contains("<!--"),
                "Output with disabled compressor should retain HTML comments");

        LOG.info("Passed");
    }

    /**
     * Test compress reduces file size compared to input.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompressReducesFileSize(@TempDir Path tempDir) throws Exception {
        LOG.info("Testing that compress reduces file size...");

        HtmlCompressor htmlCompressor = new HtmlCompressor("src/test/resources/html", tempDir.toString());
        htmlCompressor.compress();

        Path originalTemplate = Path.of("src/test/resources/html/templates/Template1.html");
        Path compressedTemplate = tempDir.resolve("templates/Template1.html");

        long originalSize = Files.size(originalTemplate);
        long compressedSize = Files.size(compressedTemplate);

        Assertions.assertTrue(compressedSize < originalSize,
                "Compressed file should be smaller than the original (original: " + originalSize + "B, compressed: "
                        + compressedSize + "B)");

        LOG.info("Passed");
    }

    /**
     * Test compress with custom file extensions processes only specified types.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompressWithCustomFileExtensions(@TempDir Path tempDir) throws Exception {
        LOG.info("Testing compress with custom file extensions...");

        // Create a test directory with mixed file types
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("page.html"), "<html><!-- comment --><body>Hello</body></html>");
        Files.writeString(srcDir.resolve("page.htm"), "<html><!-- comment --><body>World</body></html>");
        Files.writeString(srcDir.resolve("page.txt"), "this is text");

        Path targetDir = tempDir.resolve("target");
        HtmlCompressor htmlCompressor = new HtmlCompressor(srcDir.toString(), targetDir.toString());
        htmlCompressor.setFileExtensions(new String[] { "html" });
        htmlCompressor.compress();

        // Only the .html file should be processed
        Assertions.assertTrue(Files.exists(targetDir.resolve("page.html")), "HTML file should be in output");
        Assertions.assertFalse(Files.exists(targetDir.resolve("page.htm")),
                "HTM file should not be in output with 'html'-only filter");
        Assertions.assertFalse(Files.exists(targetDir.resolve("page.txt")), "TXT file should not be in output");

        LOG.info("Passed");
    }

    /**
     * Test compress with JSON file creation produces correct integration file.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompressWithJsonFileCreation(@TempDir Path tempDir) throws Exception {
        LOG.info("Testing compress with JSON file creation...");

        Path targetDir = tempDir.resolve("output");
        Path targetJsonFile = targetDir.resolve("sprite.js");

        HtmlCompressor htmlCompressor = new HtmlCompressor("src/test/resources/html", targetDir.toString(), true,
                targetJsonFile.toString(), "src/test/resources/html/integration.js");
        htmlCompressor.compress();

        Assertions.assertTrue(Files.exists(targetJsonFile), "Integration JS file should be created");
        String content = Files.readString(targetJsonFile);
        // The integration.js template contains "htmlTemplates" and "%s" gets replaced
        Assertions.assertTrue(content.contains("htmlTemplates"), "Integration file should use template structure");
        // Verify it contains references to the compressed templates
        Assertions.assertTrue(content.contains("Template1.html") || content.contains("template"),
                "Integration file should reference compressed templates");

        LOG.info("Passed");
    }

    /**
     * Test HtmlCompressor getters and setters.
     */
    @Test
    void testHtmlCompressorGettersSetters() {
        LOG.info("Testing HtmlCompressor getters and setters...");

        HtmlCompressor htmlCompressor = new HtmlCompressor("src/test/resources/html", "target/test");

        // Test srcDirPath getter/setter
        Assertions.assertEquals("src/test/resources/html", htmlCompressor.getSrcDirPath());
        htmlCompressor.setSrcDirPath("src/main/resources");
        Assertions.assertEquals("src/main/resources", htmlCompressor.getSrcDirPath());

        // Test targetDirPath getter/setter
        Assertions.assertEquals("target/test", htmlCompressor.getTargetDirPath());
        htmlCompressor.setTargetDirPath("target/other");
        Assertions.assertEquals("target/other", htmlCompressor.getTargetDirPath());

        // Test createJsonFile getter/setter
        Assertions.assertFalse(htmlCompressor.isCreateJsonFile());
        htmlCompressor.setCreateJsonFile(true);
        Assertions.assertTrue(htmlCompressor.isCreateJsonFile());

        // Test targetJsonFilePath getter/setter
        htmlCompressor.setTargetJsonFilePath("target/sprite.js");
        Assertions.assertEquals("target/sprite.js", htmlCompressor.getTargetJsonFilePath());

        // Test jsonIntegrationFilePath getter/setter
        htmlCompressor.setJsonIntegrationFilePath("src/integration.js");
        Assertions.assertEquals("src/integration.js", htmlCompressor.getJsonIntegrationFilePath());

        // Test fileEncoding getter/setter
        htmlCompressor.setFileEncoding(StandardCharsets.UTF_8);
        Assertions.assertEquals(StandardCharsets.UTF_8, htmlCompressor.getFileEncoding());

        // Test fileExtensions getter/setter
        htmlCompressor.setFileExtensions(new String[] { "html", "htm" });
        Assertions.assertArrayEquals(new String[] { "html", "htm" }, htmlCompressor.getFileExtensions());

        // Test htmlCompressor handler getter/setter
        com.googlecode.htmlcompressor.compressor.HtmlCompressor handler = new com.googlecode.htmlcompressor.compressor.HtmlCompressor();
        htmlCompressor.setHtmlCompressor(handler);
        Assertions.assertSame(handler, htmlCompressor.getHtmlCompressor());

        LOG.info("Passed");
    }

    /**
     * Test second constructor initializes all JSON-related fields.
     */
    @Test
    void testSecondConstructorInitializesJsonFields() {
        LOG.info("Testing HtmlCompressor second constructor...");

        HtmlCompressor htmlCompressor = new HtmlCompressor("src", "target", true, "target/sprite.js",
                "src/integration.js");

        Assertions.assertEquals("src", htmlCompressor.getSrcDirPath());
        Assertions.assertEquals("target", htmlCompressor.getTargetDirPath());
        Assertions.assertTrue(htmlCompressor.isCreateJsonFile());
        Assertions.assertEquals("target/sprite.js", htmlCompressor.getTargetJsonFilePath());
        Assertions.assertEquals("src/integration.js", htmlCompressor.getJsonIntegrationFilePath());

        LOG.info("Passed");
    }
}
