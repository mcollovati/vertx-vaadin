# Vertx-Vaadin-8

Vertx-Vaadin-8 is an adapter library that allow Vaadin 8.x applications to run on a Vert.x environment.

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
  <artifactId>vertx-vaadin8</artifactId>
  <version>1.0.0</version>
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
for this reason widgetset compilation is needed for projects using vertx-vaadin.
In order to do this add a `provided` dependency to the `vaadin8-sockjs` sources in project pom

```xml
<dependency>
    <groupId>com.github.mcollovati.vertx</groupId>
    <artifactId>vaadin8-sockjs</artifactId>
    <version>${vertx-vaadin8.version}</version>
    <scope>provided</scope>
    <classifier>sources</classifier>
</dependency>
```

If the project does not rely on client side addons an already compiled widgetset based on Vaadin default widgetset
can be used; to do this simply add the classifier `full` to the `vertx-vaadin8` dependency.
Remeber that `vaadin-client-compiled` dependency MUST be removed in this case.

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin8</artifactId>
  <version>1.0.0</version>
  <classifier>full</classifier>
</dependency>
```


## Getting started

To create a Vaadin application capable to run on Vertx start with a new project using Vaadin Maven archetype

```
mvn archetype:generate \
-DarchetypeGroupId=com.vaadin \
-DarchetypeArtifactId=vaadin-archetype-application \
-DarchetypeVersion=8.6.3
```

Now you have a Vaadin project with Maven build script. 
You can build the war file using `mvn install`, start the project using `mvn jetty:run` or import it into your IDE.


To make the Vaadin application ready to run on Vert.x first of all clean POM file from some servlet related stuff: 

- change project packaging type from `war` to `jar`
- remove `maven-war-plugin` and `jetty-maven-plugin`
- remove `vaadin-push` dependency
- move `src/main/webapp` content to `src/main/resources`
- edit `maven-clean-plugin` configuration to change references from `src/main/webapp` into `src/main/resources` 

Then add a dependency to `vertx-vaadin`

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin8</artifactId>
  <version>${vertx-vaadin8.version}</version>
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
 
### Default widgetset

If you do not need PUSH nor client side addons you can use the Vaadin default widgetset;
currently, due to a bug on `vertx-vaadin`, the default widgetset must be explicitly set
using `@Widgetset` or `@VaadinServletConfiguration` annotation.

For example:

```java
@VaadinServletConfiguration(..., widgetset = "com.vaadin.DefaultWidgetSet")
public static class MyUIVerticle extends VaadinVerticle {
}
```

or 

```java
@Widgetset("com.vaadin.DefaultWidgetSet")
public class MyUI extends UI {
}
```

### PUSH with default widgetset

If you do need PUSH but the project does not need other client side addons add the `full` classifier 
to the `vertx-vaadin8` dependency to use an already compiled widgetset based on Vaadin default widgetset.

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin8</artifactId>
  <version>${vertx-vaadin8.version}</version>
  <classifier>full</classifier>
</dependency>
```

Then remove `vaadin-client-compiled` dependency, remove or configure `vaadin-maven-plugin` to avoid `compile` goal execution
and annotate UI class with `@Widgetset("com.github.mcollovati.vertx.vaadin.VaadinVertxWidgetset")`.

```java
@Widgetset("com.github.mcollovati.vertx.vaadin.VaadinVertxWidgetset")
public class MyUI extends UI {

}
```

### PUSH with custom widgetset

If you do need PUSH and also other client side addons add the dependency to `vertx-vaadin` without the `full` classifier,
add the `vaadin8-sockjs` dependency as described in **Push Support** section and compile the widgetset as usually 
(see [Vaadin documentation](https://vaadin.com/docs/v8/framework/addons/addons-maven.html) for further information).

Also remember to configure `vaadin-maven-plugin` to put widgetset and theme stuff into `target/classes` directory, as in the example below

```xml
<plugin>
    <groupId>com.vaadin</groupId>
    <artifactId>vaadin-maven-plugin</artifactId>
    <version>${vaadin.plugin.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>update-theme</goal>
                <goal>update-widgetset</goal>
                <goal>compile</goal>
                <!-- Comment out compile-theme goal to use on-the-fly theme compilation -->
                <goal>compile-theme</goal>
            </goals>
            <configuration>
                <webappDirectory>${basedir}/target/classes/VAADIN/widgetsets</webappDirectory>

                <!--other configurations -->
                <extraJvmArgs>-Xmx1024M -Xss1024k</extraJvmArgs>
                <draftCompile>false</draftCompile>
                <compileReport>false</compileReport>
                <style>OBF</style>
                <strict>true</strict>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Vaadin Verticle

As next step, open the `MyUI` class and replace the custom `VaadinServlet` implementation with a new `VaadinVerticle` extension.
Annotate the verticle class with `@VaadinServletConfiguration` (at the moment Vert.x-Vaadin uses the same Vaadin 8 annotation used for servlet environment)
specifying the UI class that should be served (refer to [Vaadin documentation](https://vaadin.com/docs/v8/framework/application/application-environment.html#application.environment.parameters) for 
more information about available configuration parameters).


```java
package com.github.mcollovati.vaadin.exampleapp;

...

@Widgetset("com.github.mcollovati.vertx.vaadin.VaadinVertxWidgetset")
public class MyUI extends UI {
	...

    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIVerticle extends VaadinVerticle {
    }	
}
```


The simplest way to run and package your Vert.x Vaadin application is to use `vertx-maven-plugin`.

Open your POM file and add `vertx-maven-plugin`; 
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
                    <workDirectory>${project.build.directory}</workDirectory>
                    <verticle>com.github.mcollovati.vaadin.exampleapp.MyUI$MyUIVerticle</verticle>
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