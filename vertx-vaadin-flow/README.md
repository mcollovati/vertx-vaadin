# Vertx-Vaadin-Flow

Vertx-Vaadin-Flow is an adapter library that allow [Vaadin 10.x](https://vaadin.com/docs/v14/index.html) (or higher) applications to run on a [Vert.x environment](https://vertx.io/).

Vertx-vaadin provides a Vert.x verticle that starts an HTTP server and initialize `VertxVaadinService`, a custom implementation of [VaadinService](https://github.com/vaadin/flow/blob/master/flow-server/src/main/java/com/vaadin/flow/server/VaadinService.java).

VertxVaadinService is inspired from [VaadinServletService](https://github.com/vaadin/flow/blob/master/flow-server/src/main/java/com/vaadin/flow/server/VaadinServletService.java) and takes the same configuration parameters in the form of a `json` configuration file and from `@VaadinServletConfiguration` annotation on `VaadinVerticle` subclasses.

All [Vaadin Servlet configuration parameters](https://vaadin.com/docs/v14/flow/advanced/tutorial-all-vaadin-properties.html) can be defined in a `json` file under the `vaadin` key.

```
{
  "vaadin": {
    "ui": "com.github.mcollovati.vertx.vaadin.sample.SimpleUI",
    "heartbeatInterval": 500,
    "UIProvider": "com.ex.my.MyUIProvider"
  }
}
```

Two dependencies are needed to run Vaadin on Vert.x: `vertx-vaadin` adapter and Servlet APIs (because Vaadin relies on them, but they are not provided by Vert.x).

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>

<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>${jakarta.servlet-api.version}</version>
    <scope>runtime</scope>
</dependency>
```

## PUSH support

Vertx-Vaadin supports PUSH using a custom implementation based on SockJS that replaces the [Atmosphere stack](https://github.com/Atmosphere/atmosphere) on the client and server side.
For this reason, in addition to adding `vaadin-flow-sockjs`, Vaadin `flow-push` and `flow-client` dependencies must be excluded:

```xml
<dependnecies>
    <dependency>
        <groupId>com.github.mcollovati.vertx</groupId>
        <artifactId>vaadin-flow-sockjs</artifactId>
        <version>${vertx-vaadin-flow.version}</version>
    </dependency>

    <dependency>
        <groupId>com.vaadin</groupId>
        <artifactId>vaadin-core</artifactId>
        <exclusions>
            <exclusion>
                <groupId>com.vaadin</groupId>
                <artifactId>flow-push</artifactId>
            </exclusion>
            <exclusion>
                <groupId>com.vaadin</groupId>
                <artifactId>flow-client</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependnecies>
```

For better compatibility, use a specific version of `vaadin-flow-sockjs` targeted to  the Vaadin version in use by specifying the classifier.

```xml
<dependency>
    <groupId>com.github.mcollovati.vertx</groupId>
    <artifactId>vaadin-flow-sockjs</artifactId>
    <version>${vertx-vaadin-flow.version}</version>
    <classifier>vaadin-${vaadin.version}</classifier>
</dependency>
```

## Getting started

To create a Vaadin Flow application capable to run on Vertx start with a new project using Viritin Flow Maven archetype:

```
mvn archetype:generate  \
    -DarchetypeGroupId=in.virit  \
    -DarchetypeArtifactId=viritin-vaadin-flow-archetype  \
    -DarchetypeVersion=1.0
```

Now you have a Vaadin project with Maven build script. You can build the `jar` file using `mvn install`, start the project using `mvn vertx:run`, or import it into your IDE.

To make the Vaadin application ready to run on Vert.x, first, clean POM file from some servlet related stuff:

- change project packaging type from `war` to `jar`
- remove `maven-war-plugin` and `jetty-maven-plugin` if present
- exclude `flow-push` and `flow-client` dependencies are described in **PUSH support** section
- move `src/main/webapp` content to `src/main/resources/META-INF/resources`

Then add a dependency on `vertx-vaadin-flow`:

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>
```

Change the scope of the Servlet API dependency from 'provided' to `runtime` (or `compile` if you need to refer those APIs in your code):

```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>${jakarta.servlet-api.version}</version>
    <scope>runtime</scope>
</dependency>
```

Create a new verticle extending `VaadinVerticle`

```java
package com.github.mcollovati.vaadin.exampleapp;

public class UIVerticle extends VaadinVerticle {
}
```

Open your POM file and add the `vertx-maven-plugin` configuration. The *`<verticle>`* element inside plugin configuration must contain the fully qualified class name of your custom verticle.

For further configuration of the plugin refer to the [documentation](https://reactiverse.io/vertx-maven-plugin/).

```xml
<project>
  ...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>io.reactiverse</groupId>
                <artifactId>vertx-maven-plugin</artifactId>
                <version>1.0.20</version>
                <executions>
                    <execution>
                        <id>vmp-init-package</id>
                        <goals>
                            <goal>initialize</goal>
                            <goal>package</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <redeploy>true</redeploy>
                    <classifier>fat</classifier>
                    <attach>true</attach>
                    <stripWebJarVersion>false</stripWebJarVersion>
                    <webRoot>${project.build.outputDirectory}/META-INF/resources/webjars</webRoot>
                    <workDirectory>${project.build.directory}</workDirectory>
                    <verticle>com.github.mcollovati.vaadin.exampleapp.UIVerticle</verticle>
                </configuration>
            </plugin>
            ...
        </plugins>
    </build>
  ...
</project>
```

Build the application with `mvn package`, run it with `mvn vertx:run` and point the browser at http://localhost:8080.

You can also start the application with `java -jar target/<artifact-name>.jar`
