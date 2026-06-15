/*
 * SPDX-License-Identifier: Apache-2.0
 * See LICENSE file for details.
 *
 * Copyright 2018-2026 Hazendaz
 * Copyright 2011-2018 tunyk
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
 * The Class XmlCompressorTest.
 */
class XmlCompressorTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(XmlCompressorTest.class);

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

        XmlCompressor xmlCompressor = new XmlCompressor("src/test/resources/xml", "target/test/xmlcompressor/0");
        xmlCompressor.compress();

        // Verify output files were created
        Assertions.assertTrue(Files.exists(Path.of("target/test/xmlcompressor/0/file.xml")),
                "file.xml should be created in output");
        Assertions.assertTrue(Files.exists(Path.of("target/test/xmlcompressor/0/recursive/file.xml")),
                "recursive/file.xml should be created in output");

        // Verify content is compressed (comments removed by default)
        String compressed = Files.readString(Path.of("target/test/xmlcompressor/0/file.xml"));
        Assertions.assertFalse(compressed.contains("<!--"), "Compressed output should not contain XML comments");
        Assertions.assertTrue(compressed.contains("sometag"),
                "Compressed output should retain the XML element content");

        xmlCompressor = new XmlCompressor("src/test/resources/xml", "target/test/xmlcompressor/1");
        com.googlecode.htmlcompressor.compressor.XmlCompressor xmlCompressorHandler = new com.googlecode.htmlcompressor.compressor.XmlCompressor();
        xmlCompressorHandler.setEnabled(false);
        xmlCompressor.setXmlCompressor(xmlCompressorHandler);
        xmlCompressor.compress();

        // When compressor is disabled, output should match input (content not compressed)
        String disabledOutput = Files.readString(Path.of("target/test/xmlcompressor/1/file.xml"));
        Assertions.assertTrue(disabledOutput.contains("<!--"),
                "Output with disabled compressor should retain XML comments");

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

        XmlCompressor xmlCompressor = new XmlCompressor("src/test/resources/xml", tempDir.toString());
        xmlCompressor.compress();

        Path originalFile = Path.of("src/test/resources/xml/file.xml");
        Path compressedFile = tempDir.resolve("file.xml");

        long originalSize = Files.size(originalFile);
        long compressedSize = Files.size(compressedFile);

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

        // Create test files with different extensions
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("data.xml"),
                "<?xml version=\"1.0\"?>\n<!-- comment -->\n<root><child/></root>");
        Files.writeString(srcDir.resolve("config.cfg"), "setting=value");

        Path targetDir = tempDir.resolve("target");
        XmlCompressor xmlCompressor = new XmlCompressor(srcDir.toString(), targetDir.toString());
        xmlCompressor.setFileExtensions(new String[] { "xml" });
        xmlCompressor.compress();

        // Only the .xml file should be processed
        Assertions.assertTrue(Files.exists(targetDir.resolve("data.xml")), "XML file should be in output");
        Assertions.assertFalse(Files.exists(targetDir.resolve("config.cfg")),
                "CFG file should not be in output with XML-only filter");

        LOG.info("Passed");
    }

    /**
     * Test XmlCompressor getters and setters.
     */
    @Test
    void testXmlCompressorGettersSetters() {
        LOG.info("Testing XmlCompressor getters and setters...");

        XmlCompressor xmlCompressor = new XmlCompressor("src/test/resources/xml", "target/test");

        // Test srcDirPath getter/setter
        Assertions.assertEquals("src/test/resources/xml", xmlCompressor.getSrcDirPath());
        xmlCompressor.setSrcDirPath("src/main/resources");
        Assertions.assertEquals("src/main/resources", xmlCompressor.getSrcDirPath());

        // Test targetDirPath getter/setter
        Assertions.assertEquals("target/test", xmlCompressor.getTargetDirPath());
        xmlCompressor.setTargetDirPath("target/other");
        Assertions.assertEquals("target/other", xmlCompressor.getTargetDirPath());

        // Test fileEncoding getter/setter
        xmlCompressor.setFileEncoding(StandardCharsets.UTF_8);
        Assertions.assertEquals(StandardCharsets.UTF_8, xmlCompressor.getFileEncoding());

        // Test fileExtensions getter/setter
        xmlCompressor.setFileExtensions(new String[] { "xml", "xsd" });
        Assertions.assertArrayEquals(new String[] { "xml", "xsd" }, xmlCompressor.getFileExtensions());

        // Test xmlCompressor handler getter/setter
        com.googlecode.htmlcompressor.compressor.XmlCompressor handler = new com.googlecode.htmlcompressor.compressor.XmlCompressor();
        xmlCompressor.setXmlCompressor(handler);
        Assertions.assertSame(handler, xmlCompressor.getXmlCompressor());

        LOG.info("Passed");
    }

    /**
     * Test compress with comments removed produces smaller output.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void testCompressRemovesComments(@TempDir Path tempDir) throws Exception {
        LOG.info("Testing that compress removes XML comments...");

        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);
        String xmlWithComments = "<?xml version=\"1.0\"?>\n<!-- This is a long comment that should be removed -->\n<root>\n  <!-- Another comment -->\n  <child>value</child>\n</root>";
        Files.writeString(srcDir.resolve("test.xml"), xmlWithComments);

        Path targetDir = tempDir.resolve("target");
        XmlCompressor xmlCompressor = new XmlCompressor(srcDir.toString(), targetDir.toString());
        xmlCompressor.compress();

        String compressed = Files.readString(targetDir.resolve("test.xml"));
        Assertions.assertFalse(compressed.contains("<!--"), "Comments should be removed by default");
        Assertions.assertTrue(compressed.contains("child"), "Element content should be preserved");
        Assertions.assertTrue(compressed.contains("value"), "Text content should be preserved");

        LOG.info("Passed");
    }
}
