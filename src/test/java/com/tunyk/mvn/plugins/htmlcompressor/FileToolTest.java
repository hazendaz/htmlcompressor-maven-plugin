/*
 * SPDX-License-Identifier: Apache-2.0
 * See LICENSE file for details.
 *
 * Copyright 2018-2026 Hazendaz
 * Copyright 2011-2018 tunyk
 */
package com.tunyk.mvn.plugins.htmlcompressor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FileToolTest.
 */
class FileToolTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(FileToolTest.class);

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
     * Test get files finds the correct HTML files.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testGetFiles() throws IOException {
        LOG.info("Testing getFiles method...");

        FileTool fileTool = new FileTool("src/test/resources/html", new String[] { "htm", "html" }, true);
        Map<String, String> map = fileTool.getFiles();

        Assertions.assertEquals(3, map.size(), "Should find exactly 3 HTML files");
        Assertions.assertTrue(map.containsKey("templates/Template1.html"));
        Assertions.assertTrue(map.containsKey("templates/Template2.html"));
        Assertions.assertTrue(map.containsKey("templates/recursive/Template.html"));

        // Verify .js file is excluded by the HTML extension filter
        Assertions.assertFalse(map.containsKey("integration.js"), "JS file should not be included with HTML filter");

        LOG.info("Passed");
    }

    /**
     * Test get files content verification.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testGetFilesContent() throws IOException {
        LOG.info("Testing getFiles content verification...");

        FileTool fileTool = new FileTool("src/test/resources/html", new String[] { "htm", "html" }, true);
        Map<String, String> map = fileTool.getFiles();

        Assertions.assertTrue(map.get("templates/Template1.html").contains("template #1"),
                "Template1 content should contain 'template #1'");
        Assertions.assertTrue(map.get("templates/Template2.html").contains("template #2"),
                "Template2 content should contain 'template #2'");
        Assertions.assertTrue(map.get("templates/recursive/Template.html").contains("subfolders"),
                "Recursive template content should contain 'subfolders'");

        LOG.info("Passed");
    }

    /**
     * Test get files extension filtering excludes non-matching files.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testGetFilesExtensionFiltering() throws IOException {
        LOG.info("Testing getFiles extension filtering...");

        // Filter only JS files - should include integration.js but not HTML templates
        FileTool jsFileTool = new FileTool("src/test/resources/html", new String[] { "js" }, true);
        Map<String, String> jsMap = jsFileTool.getFiles();

        Assertions.assertEquals(1, jsMap.size(), "Should find exactly 1 JS file");
        Assertions.assertTrue(jsMap.containsKey("integration.js"), "JS file should be included with JS filter");
        Assertions.assertFalse(jsMap.containsKey("templates/Template1.html"),
                "HTML file should not be included with JS filter");

        LOG.info("Passed");
    }

    /**
     * Test write files.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testWriteFiles(@TempDir Path tempDir) throws IOException {
        LOG.info("Testing writeFiles method...");

        String targetDir = tempDir.toString();
        FileTool fileTool = new FileTool(targetDir, new String[] { "htm", "html" }, true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("file.html", "root file");
        map.put("/file2.html", "another root file");
        map.put("file3.html/", "another root file like folder");
        map.put("/template/file.html", "template file");
        map.put("template/file01.html", "template file 01");
        map.put("/template/subfolder/file.html", "template subfolder file");
        fileTool.writeFiles(map, targetDir);

        Map<String, String> files = fileTool.getFiles();
        Assertions.assertTrue(files.containsKey("file.html"));
        Assertions.assertTrue(files.containsKey("file2.html"));
        Assertions.assertTrue(files.containsKey("file3.html"));
        Assertions.assertTrue(files.containsKey("template/file.html"));
        Assertions.assertTrue(files.containsKey("template/file01.html"));
        Assertions.assertTrue(files.containsKey("template/subfolder/file.html"));

        LOG.info("Passed");
    }

    /**
     * Test write files content verification - checks that written content can be read back correctly.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testWriteFilesContentVerification(@TempDir Path tempDir) throws IOException {
        LOG.info("Testing writeFiles content verification...");

        String targetDir = tempDir.toString();
        FileTool fileTool = new FileTool(targetDir, new String[] { "html" }, true);
        Map<String, String> writeMap = new HashMap<>();
        writeMap.put("page.html", "<html><body>Hello World</body></html>");
        writeMap.put("sub/page.html", "<html><body>Sub Page</body></html>");
        fileTool.writeFiles(writeMap, targetDir);

        Map<String, String> readMap = fileTool.getFiles();
        Assertions.assertEquals("<html><body>Hello World</body></html>", readMap.get("page.html"),
                "Root file content should match what was written");
        Assertions.assertEquals("<html><body>Sub Page</body></html>", readMap.get("sub/page.html"),
                "Sub folder file content should match what was written");

        LOG.info("Passed");
    }

    /**
     * Test write to json file creates the file with expected structure.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONException
     *             the JSON exception
     */
    @Test
    void testWriteToJsonFile() throws IOException, JSONException {
        LOG.info("Testing writeToJsonFile method...");

        String targetDir = "target/test/filetool/";
        String targetFile = targetDir + "json.js";
        FileTool fileTool = new FileTool(targetDir, new String[] { "htm", "html" }, true);
        Map<String, String> map = new HashMap<String, String>();
        map.put("file.html", "root file");
        map.put("template/file.html", "template file");
        map.put("template/subfolder/file.html", "template subfolder file");
        fileTool.writeToJsonFile(map, targetFile, "var templates = \"%s\";");

        Path outputPath = Path.of(targetFile);
        Assertions.assertTrue(Files.exists(outputPath), "JSON output file should exist");
        String content = Files.readString(outputPath);
        Assertions.assertTrue(content.startsWith("var templates = "), "Content should start with var declaration");
        Assertions.assertTrue(content.contains("file.html"), "Content should contain file.html key");
        Assertions.assertTrue(content.contains("root file"), "Content should contain root file value");
        Assertions.assertTrue(content.endsWith(";"), "Content should end with semicolon");

        LOG.info("Passed");
    }

    /**
     * Test write to json file with null integration code uses default pattern.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONException
     *             the JSON exception
     */
    @Test
    void testWriteToJsonFileNullIntegrationCode(@TempDir Path tempDir) throws IOException, JSONException {
        LOG.info("Testing writeToJsonFile with null integration code...");

        String targetFile = tempDir.resolve("output.js").toString();
        FileTool fileTool = new FileTool(tempDir.toString(), new String[] { "html" }, true);
        Map<String, String> map = new HashMap<>();
        map.put("key.html", "value content");
        fileTool.writeToJsonFile(map, targetFile, null);

        Path outputPath = Path.of(targetFile);
        Assertions.assertTrue(Files.exists(outputPath), "JSON output file should exist with null integration code");
        String content = Files.readString(outputPath);
        Assertions.assertTrue(content.contains("key.html"), "Content should contain the map key");
        Assertions.assertTrue(content.contains("value content"), "Content should contain the map value");

        LOG.info("Passed");
    }

    /**
     * Test write to json file without placeholder appends it automatically.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONException
     *             the JSON exception
     */
    @Test
    void testWriteToJsonFileNoPlaceholder(@TempDir Path tempDir) throws IOException, JSONException {
        LOG.info("Testing writeToJsonFile with integration code without placeholder...");

        String targetFile = tempDir.resolve("output.js").toString();
        FileTool fileTool = new FileTool(tempDir.toString(), new String[] { "html" }, true);
        Map<String, String> map = new HashMap<>();
        map.put("key.html", "value content");
        // integrationCode without "%s" - should have "%s" appended and then replaced with JSON
        fileTool.writeToJsonFile(map, targetFile, "var data = ");

        Path outputPath = Path.of(targetFile);
        Assertions.assertTrue(Files.exists(outputPath), "JSON output file should exist");
        String content = Files.readString(outputPath);
        Assertions.assertTrue(content.startsWith("var data = "), "Content should start with var data prefix");
        Assertions.assertTrue(content.contains("key.html"), "Content should contain the map key");
        Assertions.assertTrue(content.contains("value content"), "Content should contain the map value");

        LOG.info("Passed");
    }

    /**
     * Test write to json file produces parseable JSON content.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONException
     *             the JSON exception
     */
    @Test
    void testWriteToJsonFileJsonContent(@TempDir Path tempDir) throws IOException, JSONException {
        LOG.info("Testing writeToJsonFile JSON content validity...");

        String targetFile = tempDir.resolve("output.js").toString();
        FileTool fileTool = new FileTool(tempDir.toString(), new String[] { "html" }, true);
        Map<String, String> map = new HashMap<>();
        map.put("page1.html", "<div>Content 1</div>");
        map.put("page2.html", "<div>Content 2</div>");
        // Use "\"%s\"" so the JSON object replaces the quoted placeholder, leaving pure JSON
        fileTool.writeToJsonFile(map, targetFile, "\"%s\"");

        String content = Files.readString(Path.of(targetFile));
        JSONObject parsed = new JSONObject(content);
        Assertions.assertTrue(parsed.has("page1.html"), "Parsed JSON should contain page1.html");
        Assertions.assertTrue(parsed.has("page2.html"), "Parsed JSON should contain page2.html");
        Assertions.assertEquals("<div>Content 1</div>", parsed.getString("page1.html"));
        Assertions.assertEquals("<div>Content 2</div>", parsed.getString("page2.html"));

        LOG.info("Passed");
    }

    /**
     * Test human readable byte count with SI units (base 1000).
     */
    @Test
    void testHumanReadableByteCountSI() {
        LOG.info("Testing humanReadableByteCount with SI units...");

        // Below 1 unit threshold
        Assertions.assertEquals("0 B", FileTool.humanReadableByteCount(0, true));
        Assertions.assertEquals("1 B", FileTool.humanReadableByteCount(1, true));
        Assertions.assertEquals("999 B", FileTool.humanReadableByteCount(999, true));

        // Kilobytes (1000^1)
        Assertions.assertEquals("1.0 kB", FileTool.humanReadableByteCount(1_000, true));
        Assertions.assertEquals("1.5 kB", FileTool.humanReadableByteCount(1_500, true));

        // Megabytes (1000^2)
        Assertions.assertEquals("1.0 MB", FileTool.humanReadableByteCount(1_000_000, true));

        // Gigabytes (1000^3)
        Assertions.assertEquals("1.0 GB", FileTool.humanReadableByteCount(1_000_000_000, true));

        LOG.info("Passed");
    }

    /**
     * Test human readable byte count with binary units (base 1024).
     */
    @Test
    void testHumanReadableByteCountBinary() {
        LOG.info("Testing humanReadableByteCount with binary units...");

        // Below 1 unit threshold
        Assertions.assertEquals("0 B", FileTool.humanReadableByteCount(0, false));
        Assertions.assertEquals("1 B", FileTool.humanReadableByteCount(1, false));
        Assertions.assertEquals("1023 B", FileTool.humanReadableByteCount(1_023, false));

        // Kibibytes (1024^1)
        Assertions.assertEquals("1.0 KiB", FileTool.humanReadableByteCount(1_024, false));
        Assertions.assertEquals("1.5 KiB", FileTool.humanReadableByteCount(1_536, false));

        // Mebibytes (1024^2)
        Assertions.assertEquals("1.0 MiB", FileTool.humanReadableByteCount(1_048_576, false));

        LOG.info("Passed");
    }

    /**
     * Test get elapsed HMS time for various durations.
     */
    @Test
    void testGetElapsedHMSTime() {
        LOG.info("Testing getElapsedHMSTime...");

        Assertions.assertEquals("00:00:00", FileTool.getElapsedHMSTime(0L));
        Assertions.assertEquals("00:00:01", FileTool.getElapsedHMSTime(1_000L));
        Assertions.assertEquals("00:00:59", FileTool.getElapsedHMSTime(59_000L));
        Assertions.assertEquals("00:01:00", FileTool.getElapsedHMSTime(60_000L));
        Assertions.assertEquals("00:59:59", FileTool.getElapsedHMSTime(3_599_000L));
        Assertions.assertEquals("01:00:00", FileTool.getElapsedHMSTime(3_600_000L));
        Assertions.assertEquals("01:01:01", FileTool.getElapsedHMSTime(3_661_000L));
        Assertions.assertEquals("10:30:45", FileTool.getElapsedHMSTime(37_845_000L));

        LOG.info("Passed");
    }

    /**
     * Test FileTool getters and setters.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testFileToolGettersSetters() throws IOException {
        LOG.info("Testing FileTool getters and setters...");

        FileTool fileTool = new FileTool("src/test/resources/html", new String[] { "html" }, true);

        // Test recursive flag getter/setter
        Assertions.assertTrue(fileTool.isRecursive());
        fileTool.setRecursive(false);
        Assertions.assertFalse(fileTool.isRecursive());

        // Test file extensions getter/setter
        String[] newExtensions = { "htm", "xhtml" };
        fileTool.setFileExtensions(newExtensions);
        Assertions.assertArrayEquals(newExtensions, fileTool.getFileExtensions());

        // Test root dir path getter (should be canonical/absolute)
        String rootDirPath = fileTool.getRootDirPath();
        Assertions.assertNotNull(rootDirPath);
        Assertions.assertTrue(Path.of(rootDirPath).isAbsolute(), "Root dir path should be absolute");
        Assertions.assertFalse(rootDirPath.endsWith("/"), "Root dir path should not have trailing slash");

        // Test encoding getter defaults to system charset when not set
        Assertions.assertEquals(Charset.defaultCharset(), fileTool.getFileEncoding());

        // Test encoding setter with explicit value
        fileTool.setFileEncoding(StandardCharsets.UTF_8);
        Assertions.assertEquals(StandardCharsets.UTF_8, fileTool.getFileEncoding());

        // Test encoding setter with null reverts to default charset
        fileTool.setFileEncoding(null);
        Assertions.assertEquals(Charset.defaultCharset(), fileTool.getFileEncoding());

        LOG.info("Passed");
    }

    /**
     * Test setRootDirPath normalizes paths correctly.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    void testSetRootDirPathNormalization() throws IOException {
        LOG.info("Testing setRootDirPath normalization...");

        FileTool fileTool = new FileTool("src/test/resources/html", new String[] { "html" }, true);
        String rootPath = fileTool.getRootDirPath();

        // Should be normalized to absolute canonical path
        Assertions.assertTrue(Path.of(rootPath).isAbsolute(), "Root dir path should be absolute after normalization");
        Assertions.assertFalse(rootPath.endsWith("/"), "Root dir path should not end with a trailing slash");

        // Path should not use backslashes
        Assertions.assertFalse(rootPath.contains("\\"), "Root dir path should use forward slashes, not backslashes");

        // Setting another path should update it
        fileTool.setRootDirPath("src/test/resources/xml");
        Assertions.assertTrue(fileTool.getRootDirPath().endsWith("xml"),
                "Root dir path should reflect the updated path");

        LOG.info("Passed");
    }
}
