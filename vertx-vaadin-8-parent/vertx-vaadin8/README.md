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

Vertx-Vaadin supports PUSH using a custom implementation based on SockJS that replaces the atmosphere stack on client and server side; for this reason widgetset compilation is needed for projects using vertx-vaadin.
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

If there the project does not rely on client side addons an already compiled widgetset based on Vaadin default widgetset can be used; to do this simply add the classifier `full` to the `vertx-vaadin8` dependency.

```  
 
