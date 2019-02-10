# Vertx-Vaadin-Flow

Vertx-Vaadin-Flow is an adapter library that allow Vaadin 10.x (or higher) applications to run on a Vert.x environment.

Vertx-vaadin provides a Vert.x verticle that starts an http server and initialize `VertxVaadinService`,
a custom implementation of VaadinService.

VertxVaadinService is inspired from VaadinServletService and takes the same configuration parameters in the form
of a json configuration file and from `@VaadinServletConfiguration` annotation on `VaadinVerticle` subclasses.

All [Servlet Configuration Parameter](https://vaadin.com/docs/-/part/framework/application/application-environment.html#application.environment.parameters)
 can be defined in json file under `vaadin` key.
 
```
{
  "vaadin": {
    "ui": "com.github.mcollovati.vertx.vaadin.sample.SimpleUI",
    "heartbeatInterval": 500,
    "UIProvider": "com.ex.my.MyUIProvider"
  }
}
``` 

Two dependencies are needed to run Vaadin on Vert.x: `vertx-vaadin` adapter and servlet APIs (because Vaadin relies on them
but they are not provided from Vert.x).

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>0.2.0</version>
</dependency>

<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>${javax.servlet-api.version}</version>
    <scope>runtime</scope>
</dependency>
```


## PUSH support

Vertx-Vaadin supports PUSH using a custom implementation based on SockJS that replaces the atmosphere stack on client and server side; 
for this reason Vaadin `flow-push` and `flow-client` dependencies must be excluded

```xml
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
```

For better compatibility add a dependency to the version of `vaadin-flow-sockjs`, specifying the classifier for
the vaadin version in use; for example

```xml
<dependency>
    <groupId>com.github.mcollovati.vertx</groupId>
    <artifactId>vaadin-flow-sockjs</artifactId>
    <version>0.2.0</version>
    <classifier>vaadin-${vaadin.version}</classifier>
</dependency>
```

## Getting started

To create a Vaadin Flow application capable to run on Vertx start with a new project using Viritin Flow Maven archetype

```
mvn archetype:generate  \
    -DarchetypeGroupId=in.virit  \
    -DarchetypeArtifactId=viritin-vaadin-flow-archetype  \
    -DarchetypeVersion=1.0
```

Now you have a Vaadin project with Maven build script. 
You can build the war file using `mvn install`, start the project using `mvn jetty:run` or import it into your IDE.

To make the Vaadin application ready to run on Vert.x first of all clean POM file from some servlet related stuff: 

- change project packaging type from `war` to `jar`
- remove `maven-war-plugin` and `jetty-maven-plugin` if present
- exclude `flow-push` and `flow-client` dependencies are described in **PUSH support** section
- move `src/main/webapp` content to `src/main/resources/META-INF/resources` 

Then add a dependency to `vertx-vaadin-flow`

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>
```

Change the scope of the servlet API dependency from 'provided' to `runtime` (or `compile` if you need to refer those APIs in your code). 

```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.0.1</version>
    <scope>runtime</scope>
</dependency>
```

Create a new verticle extending `VaadinVerticle`

```java
package com.github.mcollovati.vaadin.exampleapp;

public class UIVerticle extends VaadinVerticle {
}
```

Open your POM file and add `vertx-maven-plugin` configuration; 
the *`<verticle>`* element inside plugin configuration must contain the fully qualified class name of your custom verticle.
For further configurations  of vertx maven plugin refer to [documentation]((https://vmp.fabric8.io/)


```xml
<project>
	...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>io.reactiverse</groupId>
                <artifactId>vertx-maven-plugin</artifactId>
                <version>1.0.18</version>
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

Build the application with `mvn package`, run it with `mvn vertx:run` and
point the browser at http://localhost:8080. 

You can also start the application with `java -jar target/<artifact-name>.jar` 