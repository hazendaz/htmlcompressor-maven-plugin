/*
 *    Copyright 2011-2025 the original author or authors.
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

/**
 * The Class HtmlCompressor.
 */
public class HtmlCompressor {

    /** The Constant FILE_EXT. */
    private static final String[] FILE_EXT = { "htm", "html" };

    /** The file extensions. */
    private String[] fileExtensions;

    /** The src dir path. */
    private String srcDirPath;

    /** The target dir path. */
    private String targetDirPath;

    /** The file encoding. */
    private Charset fileEncoding;

    /** The create json file. */
    private boolean createJsonFile;

    /** The target json file path. */
    private String targetJsonFilePath;

    /** The json integration file path. */
    private String jsonIntegrationFilePath;

    /** The html compressor. */
    private com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressor;

    /**
     * Instantiates a new html compressor.
     *
     * @param srcDirPath
     *            the src dir path
     * @param targetDirPath
     *            the target dir path
     */
    public HtmlCompressor(String srcDirPath, String targetDirPath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
    }

    /**
     * Instantiates a new html compressor.
     *
     * @param srcDirPath
     *            the src dir path
     * @param targetDirPath
     *            the target dir path
     * @param createJsonFile
     *            the create json file
     * @param targetJsonFilePath
     *            the target json file path
     * @param jsonIntegrationFilePath
     *            the json integration file path
     */
    public HtmlCompressor(String srcDirPath, String targetDirPath, boolean createJsonFile, String targetJsonFilePath,
            String jsonIntegrationFilePath) {
        this.srcDirPath = srcDirPath;
        this.targetDirPath = targetDirPath;
        this.createJsonFile = createJsonFile;
        this.targetJsonFilePath = targetJsonFilePath;
        this.jsonIntegrationFilePath = jsonIntegrationFilePath;
    }

    /**
     * Compress.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void compress() throws IOException {
        if (fileExtensions == null || fileExtensions.length == 0) {
            fileExtensions = FILE_EXT;
        }

        FileTool fileTool = new FileTool(srcDirPath, fileExtensions, true);
        fileTool.setFileEncoding(fileEncoding);
        ConcurrentMap<String, String> map = fileTool.getFiles();

        if (htmlCompressor == null) {
            htmlCompressor = new com.googlecode.htmlcompressor.compressor.HtmlCompressor();
        }

        for (Entry<String, String> key : map.entrySet()) {
            map.put(key.getKey(), htmlCompressor.compress(key.getValue()));
        }

        fileTool.writeFiles(map, targetDirPath);
        if (createJsonFile) {
            String jsonIntegrationCode = Files.readString(Path.of(jsonIntegrationFilePath),
                    fileEncoding == null ? Charset.defaultCharset() : fileEncoding);
            fileTool.writeToJsonFile(map, targetJsonFilePath, jsonIntegrationCode);
        }
    }

    /**
     * Gets the file extensions.
     *
     * @return the file extensions
     */
    public String[] getFileExtensions() {
        return fileExtensions;
    }

    /**
     * Sets the file extensions.
     *
     * @param fileExtensions
     *            the new file extensions
     */
    public void setFileExtensions(String[] fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    /**
     * Gets the src dir path.
     *
     * @return the src dir path
     */
    public String getSrcDirPath() {
        return srcDirPath;
    }

    /**
     * Sets the src dir path.
     *
     * @param srcDirPath
     *            the new src dir path
     */
    public void setSrcDirPath(String srcDirPath) {
        this.srcDirPath = srcDirPath;
    }

    /**
     * Gets the target dir path.
     *
     * @return the target dir path
     */
    public String getTargetDirPath() {
        return targetDirPath;
    }

    /**
     * Sets the target dir path.
     *
     * @param targetDirPath
     *            the new target dir path
     */
    public void setTargetDirPath(String targetDirPath) {
        this.targetDirPath = targetDirPath;
    }

    /**
     * Gets the file encoding.
     *
     * @return the file encoding
     */
    public Charset getFileEncoding() {
        return fileEncoding;
    }

    /**
     * Sets the file encoding.
     *
     * @param fileEncoding
     *            the new file encoding
     */
    public void setFileEncoding(Charset fileEncoding) {
        this.fileEncoding = fileEncoding == null ? Charset.defaultCharset() : fileEncoding;
    }

    /**
     * Checks if is creates the json file.
     *
     * @return true, if is creates the json file
     */
    public boolean isCreateJsonFile() {
        return createJsonFile;
    }

    /**
     * Sets the creates the json file.
     *
     * @param createJsonFile
     *            the new creates the json file
     */
    public void setCreateJsonFile(boolean createJsonFile) {
        this.createJsonFile = createJsonFile;
    }

    /**
     * Gets the target json file path.
     *
     * @return the target json file path
     */
    public String getTargetJsonFilePath() {
        return targetJsonFilePath;
    }

    /**
     * Sets the target json file path.
     *
     * @param targetJsonFilePath
     *            the new target json file path
     */
    public void setTargetJsonFilePath(String targetJsonFilePath) {
        this.targetJsonFilePath = targetJsonFilePath;
    }

    /**
     * Gets the json integration file path.
     *
     * @return the json integration file path
     */
    public String getJsonIntegrationFilePath() {
        return jsonIntegrationFilePath;
    }

    /**
     * Sets the json integration file path.
     *
     * @param jsonIntegrationFilePath
     *            the new json integration file path
     */
    public void setJsonIntegrationFilePath(String jsonIntegrationFilePath) {
        this.jsonIntegrationFilePath = jsonIntegrationFilePath;
    }

    /**
     * Gets the html compressor.
     *
     * @return the html compressor
     */
    public com.googlecode.htmlcompressor.compressor.HtmlCompressor getHtmlCompressor() {
        return htmlCompressor;
    }

    /**
     * Sets the html compressor.
     *
     * @param htmlCompressor
     *            the new html compressor
     */
    public void setHtmlCompressor(com.googlecode.htmlcompressor.compressor.HtmlCompressor htmlCompressor) {
        this.htmlCompressor = htmlCompressor;
    }
}
