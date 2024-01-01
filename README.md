# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.github.mcollovati.vertx/vertx-vaadin-flow.svg?label=vertx-vaadin-flow)
![Repsy Snapshots](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.repsy.io%2Fmvn%2Fmcollovati%2Fvertx-vaadin-snapshots%2Fcom%2Fgithub%2Fmcollovati%2Fvertx%2Fvertx-vaadin-flow%2Fmaven-metadata.xml&label=repsy%20(snapshots))


## Description

Vertx Vaadin is an adapter library that lets you run [Vaadin](https://vaadin.com/) applications on top of [Vert.x](http://vertx.io/).
This means you can mix the simplicity and robustness of Vaadin applications with the powerful tools provided by Vert.x, such as event bus, clustering, High Availability and Fail-Over.

> [!IMPORTANT]
> Vaadin 8 is not supported anymore.

## Installation and Getting Started

Vertx-vaadin binaries are available on Maven Central and Bintray.

### Maven

Stable artifacts are published on Maven Central.

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>
```

For better compatibility with Flow client, specific `vaadin-flow-sockjs` artifacts targeting exact Vaadin versions
in use are published using the `vaadin-${vaadin.version}` classifier.

```xml
<dependency>
    <groupId>com.github.mcollovati.vertx</groupId>
    <artifactId>vaadin-flow-sockjs</artifactId>
    <version>${vertx-vaadin-flow.version}</version>
    <classifier>vaadin-${vaadin.version}</classifier>
</dependency>
```

Snapshots and `vaadin-flow-sockjs` classifiers are currently published on [Repsy](https://repsy.io/).

```xml
<repositories>
    <repository>
        <id>vertx-vaadin</id>
        <url>https://repo.repsy.io/mvn/mcollovati/vertx-vaadin</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>	
    </repository>
    <repository>
    <id>vertx-vaadin-snapshots</id>
        <url>https://repo.repsy.io/mvn/mcollovati/vertx-vaadin-snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```



## Compatibility matrix

| Vaadin version | Vert.x version | vertx-vaadin version     | Status                                                                                                                                                            |
|----------------|----------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 23.3           | 4.3.1+         | vertx-vaadin-flow:23.4.x | ![Development](https://github.com/mcollovati/vertx-vaadin/actions/workflows/validation.yml/badge.svg?event=push&branch=development) |

## Documentation

See [vertx-vaadin-flow](vertx-vaadin-flow-parent/vertx-vaadin-flow) module for more information.

## Demo and samples

Source code for sample application can be found on [vaadin-vertx-samples](https://github.com/mcollovati/vaadin-vertx-samples) repository. 

## Issue tracking
  
The issues for this project are tracked on its [github.com page](https://github.com/mcollovati/vertx-vaadin/issues). All bug reports and feature requests are appreciated.
  
## Contributions
  
Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:
- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it
  
## License

Vertx Vaadin is distributed under MIT License. For license terms, see [LICENSE](LICENSE).

## Acknowledgements

Thanks to:
 
* [David Sowerby](https://github.com/davidsowerby) (author of [Krail framework](https://github.com/davidsowerby/krail)) for all his precious technical and non technical support.
* [Vaadin](https://vaadin.com/), and especially [Pekka Hyvönen](https://twitter.com/plekuu), for allowing me to use
Flow UI test code and for the [Testbench](https://vaadin.com/testbench) license.
* [Dudeplayz](https://github.com/Dudeplayz) for his valuable feedback.
