HTMLCompressor Maven Plugin Examples
====================================

Using ```fileExtensions``` property
-----------------------------------

If you want to compress jsp files as well (or any other file type, this is just for example), then you need to update your pom file like below:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.hazendaz.maven</groupId>
      <artifactId>htmlcompressor-maven-plugin</artifactId>
      <version>2.1.1-SNAPSHOT</version>
      <configuration>
        <goalPrefix>htmlcompressor</goalPrefix>
        <fileExtensions>
          <fileExtension>htm</fileExtension>
          <fileExtension>html</fileExtension>
          <fileExtension>jsp</fileExtension>
        </fileExtensions>
      </configuration>
    </plugin>
  </plugins>
</build>
```

All other file types will be ignored during processing.

For more details about configuration description please refer [htmlcompressor](https://github.com/hazendaz/htmlcompressor/) site.
