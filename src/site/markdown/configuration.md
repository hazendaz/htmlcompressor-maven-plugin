=== HTML compression plugin configuration ===
{|
!Property
!Default Value
!Description
|-
|enabled
|true
|if false all compression is off
|-
|removeComments
|true
|if false keeps HTML comments
|-
|removeMultiSpaces
|true
|if false keeps multiple whitespace characters
|-
|removeIntertagSpaces
|false
|removes iter-tag whitespace characters
|-
|removeQuotes
|false
|removes unnecessary tag attribute quotes
|-
|simpleDoctype
|false
|simplify existing doctype
|-
|removeScriptAttributes
|false
|remove optional attributes from script tags
|-
|removeStyleAttributes
|false
|remove optional attributes from style tags
|-
|removeLinkAttributes
|false
|remove optional attributes from link tags
|-
|removeFormAttributes
|false
|remove optional attributes from form tags
|-
|removeInputAttributes
|false
|remove optional attributes from input tags
|-
|simpleBooleanAttributes
|false
|remove values from boolean tag attributes
|-
|removeJavaScriptProtocol
|false
|remove "javascript:" from inline event handlers
|-
|removeHttpProtocol
|false
|replace "http://" with "//" inside tag attributes
|-
|removeHttpsProtocol
|false
|replace "https://" with "//" inside tag attributes
|-
|compressCss
|false
|compress inline css
|-
|yuiCssLineBreak
| -1
| --line-break param for Yahoo YUI Compressor
|-
|compressJavaScript
|false
|compress inline javascript
|-
|jsCompressor
|yui
|javascript compression: "yui" or "closure"
|-
|yuiJsNoMunge
|false
| --nomunge param for Yahoo YUI Compressor
|-
|yuiJsPreserveAllSemiColons
|false
| --preserve-semi param for Yahoo YUI Compressor
|-
|yuiJsLineBreak
| -1
| --line-break param for Yahoo YUI Compressor
|-
| closureOptLevel
|simple
|closureOptLevel = "simple", "advanced" or "whitespace"
|-
|yuiJsDisableOptimizations
|false
| --disable-optimizations param for Yahoo YUI Compressor
|-
|predefinedPreservePatterns
|
|predefined patterns for most often used custom preservation rules: PHP_TAG_PATTERN and SERVER_SCRIPT_TAG_PATTERN.
|-
|preservePatterns
|
|preserve patterns
|-
|preservePatternFiles
|
|list of files containing preserve patterns
|-
|generateStatistics
|true
|HTML compression statistics
|-
|srcFolder
|src/main/resources/html
| source folder where html files are located.
|-
|targetFolder
|target/htmlcompressor/html
|target folder where compressed html files will be placed.
|-
|javascriptHtmlSprite
|true
|Create javascript file which includes all compressed html files as json object. If set to true then javascriptHtmlSpriteIntegrationFile param is required, otherwise it will throw exception.
|-
|javascriptHtmlSpriteIntegrationFile
|src/main/resources/html/integration.js
|JavaScript sprite integration file (first occurrence of "%s" will be substituted by json with allncompressed html strings)
|-
|javascriptHtmlSpriteTargetFile
|target/htmlcompressor/html/integration.js
|The target JavaScript sprite file with compressed html files as json object.
|-
|encoding
|utf-8
|Charset encoding for files to read and create
|-
|closureCustomExternsOnly
|false
|Disable default built-in closure externs.
|-
|closureExterns
|
|Sets custom closure externs file list.
|-
|fileExt
|htm,html
|File types to be processed.
|-
|htmlCompressionStatistics
|target/htmlcompressor/html-compression-statistics.txt
|File where statistics of html compression is stored.
|}

=== XML compression plugin configuration ===
{|
!Property
!Default Value
!Description
|-
|enabled
|true
|if false all compression is off
|-
|removeComments
|true
|if false keeps XML comments
|-
|removeIntertagSpaces
|true
|removes iter-tag whitespace characters
|-
|srcFolder
|src/main/resources/xml
|source folder where xml files are located.
|-
|targetFolder
|target/htmlcompressor/xml
|target folder where compressed xml files will be placed.
|-
|encoding
|utf-8
|Charset encoding for files to read and create
|-
|fileExt
|xml
|File types to be processed.
|}

=== Example of using <tt>fileExt</tt> property ===

If you want to compress jsp files as well (or any other file type, this is just for example), then you need to update your pom file like below:
<pre>
<build>
  <plugins>
    <plugin>
      <groupId>com.github.hazendaz.maven</groupId>
      <artifactId>htmlcompressor-maven-plugin</artifactId>
      <version>2.1.1-SNAPSHOT</version>
      <configuration>
        <goalPrefix>htmlcompressor</goalPrefix>
        <fileExt>
          <fileExt>htm</fileExt>
          <fileExt>html</fileExt>
          <fileExt>jsp</fileExt>
        </fileExt>
      </configuration>
    </plugin>
  </plugins>
</build>
</pre>

All other file types will be ignored during processing.

For more details about configuration description please refer [https://github.com/hazendaz/htmlcompressor/ htmlcompressor] site.
