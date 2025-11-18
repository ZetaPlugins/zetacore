# zetacore

zetacore is the core library for plugins made by ZetaPlugins.

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
