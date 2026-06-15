/*
 * SPDX-License-Identifier: Apache-2.0
 * See LICENSE file for details.
 *
 * Copyright 2018-2026 Hazendaz
 * Copyright 2011-2018 tunyk
 */
package com.tunyk.mvn.plugins.htmlcompressor;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class XmlCompressorMojoTest.
 */
class XmlCompressorMojoTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(XmlCompressorMojoTest.class);

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
        LOG.info("Mojo test finished.");
    }

    /**
     * Sets the up.
     */
    @BeforeEach
    void setUp() {
        LOG.info("Setting up data for testing...");
    }

    /**
     * Test execute with default settings produces output files.
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecute() throws MojoExecutionException {
        LOG.info("Testing mojo execution...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder("target/htmlcompressor/xml");
        xmlCompressorMojo.execute();

        // Verify output files were produced
        Assertions.assertTrue(Files.exists(Path.of("target/htmlcompressor/xml/file.xml")),
                "file.xml should be created in target folder");
        Assertions.assertTrue(Files.exists(Path.of("target/htmlcompressor/xml/recursive/file.xml")),
                "recursive/file.xml should be created in target folder");

        LOG.info("Passed");
    }

    /**
     * Test execute when skip is true produces no output.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteSkip(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with skip=true...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder(tempDir.toString());
        xmlCompressorMojo.setSkip(true);
        xmlCompressorMojo.execute();

        // No files should be created in the target directory when skipped
        Assertions.assertFalse(Files.exists(tempDir.resolve("file.xml")),
                "No output files should be created when skip=true");

        LOG.info("Passed");
    }

    /**
     * Test execute when enabled is false produces no output.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteDisabled(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with enabled=false...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder(tempDir.toString());
        xmlCompressorMojo.setEnabled(false);
        xmlCompressorMojo.execute();

        // No files should be created when compression is disabled
        Assertions.assertFalse(Files.exists(tempDir.resolve("file.xml")),
                "No output files should be created when enabled=false");

        LOG.info("Passed");
    }

    /**
     * Test execute with deprecated fileExt parameter.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    @SuppressWarnings("deprecation")
    void testExecuteWithDeprecatedFileExt(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with deprecated fileExt parameter...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder(tempDir.toString());
        // Use deprecated fileExt (should fall through to fileExtensions when fileExtensions is null)
        xmlCompressorMojo.setFileExt(new String[] { "xml" });
        xmlCompressorMojo.execute();

        // Verify output files were produced using the deprecated extension list
        Assertions.assertTrue(Files.exists(tempDir.resolve("file.xml")),
                "file.xml should be in output when using deprecated fileExt");

        LOG.info("Passed");
    }

    /**
     * Test execute compresses XML and removes comments.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteCompressesXml(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution compresses XML content...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder(tempDir.toString());
        xmlCompressorMojo.execute();

        // Verify compression occurred (comments should be removed by default)
        Assertions.assertTrue(Files.exists(tempDir.resolve("file.xml")), "file.xml should exist in target");
        Assertions.assertDoesNotThrow(() -> {
            String compressedContent = Files.readString(tempDir.resolve("file.xml"));
            Assertions.assertFalse(compressedContent.contains("<!--"), "Compressed XML should not contain comments");
            Assertions.assertTrue(compressedContent.contains("sometag"),
                    "Compressed XML should retain element content");
        });

        LOG.info("Passed");
    }

    /**
     * Test XmlCompressorMojo getters and setters for all configurable parameters.
     */
    @Test
    @SuppressWarnings("deprecation")
    void testXmlMojoGettersSetters() {
        LOG.info("Testing XmlCompressorMojo getters and setters...");

        XmlCompressorMojo mojo = new XmlCompressorMojo();

        // Skip
        Assertions.assertFalse(mojo.isSkip());
        mojo.setSkip(true);
        Assertions.assertTrue(mojo.isSkip());

        // Enabled
        Assertions.assertTrue(mojo.getEnabled());
        mojo.setEnabled(false);
        Assertions.assertFalse(mojo.getEnabled());

        // Remove comments
        Assertions.assertTrue(mojo.getRemoveComments());
        mojo.setRemoveComments(false);
        Assertions.assertFalse(mojo.getRemoveComments());

        // Remove intertag spaces
        Assertions.assertTrue(mojo.getRemoveIntertagSpaces());
        mojo.setRemoveIntertagSpaces(false);
        Assertions.assertFalse(mojo.getRemoveIntertagSpaces());

        // Source folder
        mojo.setSrcFolder("src/main/resources");
        Assertions.assertEquals("src/main/resources", mojo.getSrcFolder());

        // Target folder
        mojo.setTargetFolder("target/classes");
        Assertions.assertEquals("target/classes", mojo.getTargetFolder());

        // File extensions
        mojo.setFileExtensions(new String[] { "xml", "xsd" });
        Assertions.assertArrayEquals(new String[] { "xml", "xsd" }, mojo.getFileExtensions());

        // Deprecated fileExt getter/setter
        mojo.setFileExt(new String[] { "xml" });
        Assertions.assertArrayEquals(new String[] { "xml" }, mojo.getFileExt());

        LOG.info("Passed");
    }

    /**
     * Test execute with remove comments disabled retains comments.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteWithCommentsRetained(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with removeComments=false...");

        XmlCompressorMojo xmlCompressorMojo = new XmlCompressorMojo();
        xmlCompressorMojo.setSrcFolder("src/test/resources/xml");
        xmlCompressorMojo.setTargetFolder(tempDir.toString());
        xmlCompressorMojo.setRemoveComments(false);
        xmlCompressorMojo.execute();

        Assertions.assertTrue(Files.exists(tempDir.resolve("file.xml")), "file.xml should exist in target");
        Assertions.assertDoesNotThrow(() -> {
            String content = Files.readString(tempDir.resolve("file.xml"));
            // With removeComments=false, comments should be preserved
            Assertions.assertTrue(content.contains("<!--"),
                    "XML comments should be retained when removeComments=false");
        });

        LOG.info("Passed");
    }
}
