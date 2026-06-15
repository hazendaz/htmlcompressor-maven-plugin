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
 * The Class HtmlCompressorMojoTest.
 */
class HtmlCompressorMojoTest {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HtmlCompressorMojoTest.class);

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
     * Test execute with default settings produces output files and statistics.
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecute() throws MojoExecutionException {
        LOG.info("Testing mojo execution...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setJavascriptHtmlSpriteIntegrationFile("src/test/resources/html/integration.js");
        htmlCompressorMojo.setTargetFolder("target/htmlcompressor/html");
        htmlCompressorMojo.execute();

        // Verify output files were produced
        Assertions.assertTrue(Files.exists(Path.of("target/htmlcompressor/html/templates/Template1.html")),
                "Compressed Template1.html should exist in target folder");
        Assertions.assertTrue(Files.exists(Path.of("target/htmlcompressor/html/templates/Template2.html")),
                "Compressed Template2.html should exist in target folder");

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

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setSkip(true);
        htmlCompressorMojo.execute();

        // No files should be created in the target directory when skipped
        Assertions.assertFalse(Files.exists(tempDir.resolve("templates/Template1.html")),
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

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setEnabled(false);
        htmlCompressorMojo.execute();

        // No files should be created when compression is disabled
        Assertions.assertFalse(Files.exists(tempDir.resolve("templates/Template1.html")),
                "No output files should be created when enabled=false");

        LOG.info("Passed");
    }

    /**
     * Test execute when src folder does not exist produces no output and no exception.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteNonExistentSrcFolder(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with non-existent source folder...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("/nonexistent/path/to/resources");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());

        // Should not throw an exception; just log a warning and return
        Assertions.assertDoesNotThrow(htmlCompressorMojo::execute,
                "Execution should not throw when source folder does not exist");

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

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setJavascriptHtmlSprite(false);
        // Set deprecated fileExt parameter (should be used when fileExtensions is null)
        htmlCompressorMojo.setFileExt(new String[] { "html" });
        htmlCompressorMojo.execute();

        // Files should be processed using the deprecated extension list
        Assertions.assertTrue(Files.exists(tempDir.resolve("templates/Template1.html")),
                "Template1.html should be in output when using deprecated fileExt");

        LOG.info("Passed");
    }

    /**
     * Test execute with javascript html sprite disabled.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteWithJavascriptSpriteDisabled(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with javascript sprite disabled...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setJavascriptHtmlSprite(false);
        htmlCompressorMojo.setHtmlCompressionStatistics(tempDir.resolve("statistics.txt").toString());
        htmlCompressorMojo.execute();

        // HTML files should be compressed
        Assertions.assertTrue(Files.exists(tempDir.resolve("templates/Template1.html")),
                "Template1.html should be compressed to target folder");
        // No integration JS file should be created
        Assertions.assertFalse(Files.exists(tempDir.resolve("integration.js")),
                "Integration JS should not be created when sprite is disabled");

        LOG.info("Passed");
    }

    /**
     * Test HtmlCompressorMojo getters and setters for all configurable parameters.
     */
    @Test
    void testHtmlMojoGettersSetters() {
        LOG.info("Testing HtmlCompressorMojo getters and setters...");

        HtmlCompressorMojo mojo = new HtmlCompressorMojo();

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

        // Remove multi-spaces
        Assertions.assertTrue(mojo.getRemoveMultiSpaces());
        mojo.setRemoveMultiSpaces(false);
        Assertions.assertFalse(mojo.getRemoveMultiSpaces());

        // Remove intertag spaces
        Assertions.assertFalse(mojo.getRemoveIntertagSpaces());
        mojo.setRemoveIntertagSpaces(true);
        Assertions.assertTrue(mojo.getRemoveIntertagSpaces());

        // Remove quotes
        Assertions.assertFalse(mojo.getRemoveQuotes());
        mojo.setRemoveQuotes(true);
        Assertions.assertTrue(mojo.getRemoveQuotes());

        // Simple doctype
        Assertions.assertFalse(mojo.getSimpleDoctype());
        mojo.setSimpleDoctype(true);
        Assertions.assertTrue(mojo.getSimpleDoctype());

        // Remove script attributes
        Assertions.assertFalse(mojo.getRemoveScriptAttributes());
        mojo.setRemoveScriptAttributes(true);
        Assertions.assertTrue(mojo.getRemoveScriptAttributes());

        // Remove style attributes
        Assertions.assertFalse(mojo.getRemoveStyleAttributes());
        mojo.setRemoveStyleAttributes(true);
        Assertions.assertTrue(mojo.getRemoveStyleAttributes());

        // Remove link attributes
        Assertions.assertFalse(mojo.getRemoveLinkAttributes());
        mojo.setRemoveLinkAttributes(true);
        Assertions.assertTrue(mojo.getRemoveLinkAttributes());

        // Remove form attributes
        Assertions.assertFalse(mojo.getRemoveFormAttributes());
        mojo.setRemoveFormAttributes(true);
        Assertions.assertTrue(mojo.getRemoveFormAttributes());

        // Remove input attributes
        Assertions.assertFalse(mojo.getRemoveInputAttributes());
        mojo.setRemoveInputAttributes(true);
        Assertions.assertTrue(mojo.getRemoveInputAttributes());

        // Simple boolean attributes
        Assertions.assertFalse(mojo.getSimpleBooleanAttributes());
        mojo.setSimpleBooleanAttributes(true);
        Assertions.assertTrue(mojo.getSimpleBooleanAttributes());

        // Remove javascript protocol
        Assertions.assertFalse(mojo.getRemoveJavaScriptProtocol());
        mojo.setRemoveJavaScriptProtocol(true);
        Assertions.assertTrue(mojo.getRemoveJavaScriptProtocol());

        // Remove HTTP protocol
        Assertions.assertFalse(mojo.getRemoveHttpProtocol());
        mojo.setRemoveHttpProtocol(true);
        Assertions.assertTrue(mojo.getRemoveHttpProtocol());

        // Remove HTTPS protocol
        Assertions.assertFalse(mojo.getRemoveHttpsProtocol());
        mojo.setRemoveHttpsProtocol(true);
        Assertions.assertTrue(mojo.getRemoveHttpsProtocol());

        // Compress CSS
        Assertions.assertFalse(mojo.getCompressCss());
        mojo.setCompressCss(true);
        Assertions.assertTrue(mojo.getCompressCss());

        // Preserve line breaks
        Assertions.assertFalse(mojo.getPreserveLineBreaks());
        mojo.setPreserveLineBreaks(true);
        Assertions.assertTrue(mojo.getPreserveLineBreaks());

        // Compress JavaScript
        Assertions.assertFalse(mojo.getCompressJavaScript());
        mojo.setCompressJavaScript(true);
        Assertions.assertTrue(mojo.getCompressJavaScript());

        // JS compressor type
        Assertions.assertEquals("yui", mojo.getJsCompressor());
        mojo.setJsCompressor("closure");
        Assertions.assertEquals("closure", mojo.getJsCompressor());

        // Closure optimization level
        Assertions.assertEquals("simple", mojo.getClosureOptLevel());
        mojo.setClosureOptLevel("advanced");
        Assertions.assertEquals("advanced", mojo.getClosureOptLevel());

        // Generate statistics
        Assertions.assertTrue(mojo.getGenerateStatistics());
        mojo.setGenerateStatistics(false);
        Assertions.assertFalse(mojo.getGenerateStatistics());

        // Source and target folders
        mojo.setSrcFolder("src/main/webapp");
        Assertions.assertEquals("src/main/webapp", mojo.getSrcFolder());
        mojo.setTargetFolder("target/compressed");
        Assertions.assertEquals("target/compressed", mojo.getTargetFolder());

        // Javascript sprite settings
        Assertions.assertTrue(mojo.getJavascriptHtmlSprite());
        mojo.setJavascriptHtmlSprite(false);
        Assertions.assertFalse(mojo.getJavascriptHtmlSprite());

        mojo.setJavascriptHtmlSpriteIntegrationFile("src/integration.js");
        Assertions.assertEquals("src/integration.js", mojo.getJavascriptHtmlSpriteIntegrationFile());

        mojo.setJavascriptHtmlSpriteTargetFile("target/sprite.js");
        Assertions.assertEquals("target/sprite.js", mojo.getJavascriptHtmlSpriteTargetFile());

        // Encoding
        mojo.setEncoding("ISO-8859-1");
        Assertions.assertEquals("ISO-8859-1", mojo.getEncoding());

        // HTML compression statistics file
        mojo.setHtmlCompressionStatistics("target/stats.txt");
        Assertions.assertEquals("target/stats.txt", mojo.getHtmlCompressionStatistics());

        // File extensions
        mojo.setFileExt(new String[] { "xhtml" });
        Assertions.assertArrayEquals(new String[] { "xhtml" }, mojo.getFileExt());
        mojo.setFileExtensions(new String[] { "html", "htm" });
        Assertions.assertArrayEquals(new String[] { "html", "htm" }, mojo.getFileExtensions());

        // YUI CSS line break
        Assertions.assertEquals(-1, mojo.getYuiCssLineBreak());
        mojo.setYuiCssLineBreak(120);
        Assertions.assertEquals(120, mojo.getYuiCssLineBreak());

        // Closure custom externs only
        Assertions.assertFalse(mojo.getClosureCustomExternsOnly());
        mojo.setClosureCustomExternsOnly(true);
        Assertions.assertTrue(mojo.getClosureCustomExternsOnly());

        // YUI JS configuration
        Assertions.assertFalse(mojo.getYuiJsNoMunge());
        mojo.setYuiJsNoMunge(true);
        Assertions.assertTrue(mojo.getYuiJsNoMunge());

        Assertions.assertFalse(mojo.getYuiJsPreserveAllSemiColons());
        mojo.setYuiJsPreserveAllSemiColons(true);
        Assertions.assertTrue(mojo.getYuiJsPreserveAllSemiColons());

        Assertions.assertEquals(-1, mojo.getYuiJsLineBreak());
        mojo.setYuiJsLineBreak(200);
        Assertions.assertEquals(200, mojo.getYuiJsLineBreak());

        Assertions.assertFalse(mojo.getYuiJsDisableOptimizations());
        mojo.setYuiJsDisableOptimizations(true);
        Assertions.assertTrue(mojo.getYuiJsDisableOptimizations());

        // Preserve pattern arrays
        mojo.setPreservePatterns(new String[] { "<span>.*?</span>" });
        Assertions.assertArrayEquals(new String[] { "<span>.*?</span>" }, mojo.getPreservePatterns());

        // Closure externs
        mojo.setClosureExterns(new String[] { "externs1.js", "externs2.js" });
        Assertions.assertArrayEquals(new String[] { "externs1.js", "externs2.js" }, mojo.getClosureExterns());

        LOG.info("Passed");
    }

    /**
     * Test execute with predefined preserve patterns (PHP_TAG_PATTERN).
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteWithPredefinedPreservePatterns(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with predefined preserve patterns...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setJavascriptHtmlSprite(false);
        htmlCompressorMojo.setHtmlCompressionStatistics(tempDir.resolve("statistics.txt").toString());
        htmlCompressorMojo
                .setPredefinedPreservePatterns(new String[] { "PHP_TAG_PATTERN", "SERVER_SCRIPT_TAG_PATTERN" });
        htmlCompressorMojo.execute();

        // Files should still be created successfully
        Assertions.assertTrue(Files.exists(tempDir.resolve("templates/Template1.html")),
                "Template1.html should exist in output when using predefined preserve patterns");

        LOG.info("Passed");
    }

    /**
     * Test execute with custom preserve patterns.
     *
     * @param tempDir
     *            the temp dir provided by JUnit
     *
     * @throws MojoExecutionException
     *             the mojo execution exception
     */
    @Test
    void testExecuteWithCustomPreservePatterns(@TempDir Path tempDir) throws MojoExecutionException {
        LOG.info("Testing mojo execution with custom preserve patterns...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder(tempDir.toString());
        htmlCompressorMojo.setJavascriptHtmlSprite(false);
        htmlCompressorMojo.setHtmlCompressionStatistics(tempDir.resolve("statistics.txt").toString());
        htmlCompressorMojo.setPreservePatterns(new String[] { "<div[^>]*>.*?</div>" });
        htmlCompressorMojo.execute();

        // Files should still be created successfully
        Assertions.assertTrue(Files.exists(tempDir.resolve("templates/Template1.html")),
                "Template1.html should exist in output when using custom preserve patterns");

        LOG.info("Passed");
    }

    /**
     * Test execute with invalid preserve pattern throws MojoExecutionException.
     */
    @Test
    void testExecuteWithInvalidPreservePatternThrows() {
        LOG.info("Testing mojo execution with invalid preserve pattern...");

        HtmlCompressorMojo htmlCompressorMojo = new HtmlCompressorMojo();
        htmlCompressorMojo.setSrcFolder("src/test/resources/html");
        htmlCompressorMojo.setTargetFolder("target/test/invalid-pattern");
        htmlCompressorMojo.setJavascriptHtmlSprite(false);
        // Invalid regex pattern should cause MojoExecutionException
        htmlCompressorMojo.setPreservePatterns(new String[] { "[invalid-regex(" });

        Assertions.assertThrows(MojoExecutionException.class, htmlCompressorMojo::execute,
                "Should throw MojoExecutionException for invalid preserve pattern regex");

        LOG.info("Passed");
    }
}
