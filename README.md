HTMLCompressor Maven Plugin
===========================

[![Java CI](https://github.com/hazendaz/htmlcompressor-maven-plugin/actions/workflows/ci.yaml/badge.svg)](https://github.com/hazendaz/htmlcompressor-maven-plugin/actions/workflows/ci.yaml)
[![Coverage Status](https://coveralls.io/repos/github/hazendaz/htmlcompressor-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/hazendaz/htmlcompressor-maven-plugin?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hazendaz.maven/htmlcompressor-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.hazendaz.maven/htmlcompressor-maven-plugin)
[![Apache2](<http://img.shields.io/badge/license-Apache%202-blue.svg>)](<http://www.apache.org/licenses/LICENSE-2.0>)

![hazendaz](src/site/resources/images/hazendaz-banner.jpg)

Overview
--------

Maven HTMLCompressor Plugin allows to compress HTML/XML files by adding a few lines to the pom file.
This plugin uses [htmlcompressor](https://github.com/hazendaz/htmlcompressor) library.

Configuration Site
------------------

See site page for configuration usage and plugin setup [here](https://hazendaz.github.io/htmlcompressor-maven-plugin/).

'javax' branch is for javax namespace.

Getting started
---------------

The simplest way to start using this plugin is:

1. Enable plugin in your pom.xml

a. javax namespace jsp usage

``` xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.hazendaz.maven</groupId>
            <artifactId>htmlcompressor-maven-plugin</artifactId>
            <version>1.10.1</version>
            <configuration>
                <goalPrefix>htmlcompressor</goalPrefix>
            </configuration>
        </plugin>
    </plugins>
</build>
```

b. jakarta namespace jsp usage or any other usage if not using jsp's

``` xml
<build>
    <plugins>
        <plugin>
            <groupId>com.github.hazendaz.maven</groupId>
            <artifactId>htmlcompressor-maven-plugin</artifactId>
            <version>2.1.1</version>
            <configuration>
                <goalPrefix>htmlcompressor</goalPrefix>
            </configuration>
        </plugin>
    </plugins>
</build>
```

2. Put XML and HTML files under `src/main/resources` into any underlying
structure as HTMLCompressor will recursively process files

3. For HTML compression, create `integration.js` file under
`src/main/resources` where html is stored with the contents like below.
It will integrate HTML templates into JavaScript ("%s" will be replaced
with JSON object and copied to the target folder).

``` java
var htmlTemplatesInjector = {
    htmlTemplates: "%s"
};
```

4. Run maven goals:

```
mvn htmlcompressor:html
mvn htmlcompressor:xml
```

5. Check the target folder for output where resources are stored.

Bug reports, feature requests, and general inquiries welcome.
