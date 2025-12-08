# zetacore

zetacore is the core library for plugins made by ZetaPlugins.

-> [javadocs](https://jd.zetacore.zetaplugins.com)

## Overview

The **zetacore** project is organized into several packages, each responsible for a specific part of the library:

- **`commands`** – Core command framework.
- **`debug`** – Utilities for generating debug reports and communicating with `debug.zetaplugins.com`.
- **`services.bstats`** – Convenience wrapper for bStats metrics.
- **`services.commands`** – Components related to command registration.
- **`services.config`** – Tools for managing and reading configuration files.
- **`services.di`** – Dependency injection framework (annotations are found in `annotations`).
- **`services.events`** – Classes for registering and managing event listeners.
- **`services.localization`** – Localization utilities.
- **`services.messages`** – Message formatting utilities.
- **`services.updatechecker`** – Prebuilt update checker implementations.
- **`annotations`** – Annotations for the DI framework and auto-registration helpers.

## Usage

### 1. Include zetacore as a dependency

#### Maven

```xml
<repositories>
    <repository>
        <id>zetaplugins</id>
        <url>https://maven.zetaplugins.com/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.zetaplugins</groupId>
        <artifactId>zetacore</artifactId>
        <version>{latest_version}</version>
    </dependency>
</dependencies>
```

#### Gradle

```gradle
repositories {
    maven { url 'https://maven.zetaplugins.com/' }
}

dependencies {
    implementation 'com.zetaplugins:zetacore:{latest_version}'
}
```

### 2. Shade into the plugin jar

#### Maven

To shade zetacore into the plugin jar, add the following to the plugins section of the `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <artifactSet>
            <includes>
                <include>com.zetaplugins:zetacore</include>
            </includes>
        </artifactSet>
        <transformers>
            <transformer
                    implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
            <transformer
                    implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>{main_class_path}</mainClass>
            </transformer>
        </transformers>
    </configuration>
</plugin>
```

#### Gradle

Idk figure it out yourself lol

### 3. Read the javadocs

You can find the javadocs [here](https://jd.zetacore.zetaplugins.com/).

Usage examples and guides will be added in the future. For now, please refer to the javadocs for information on how to use the library.

## License

zetacore is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for more details.
