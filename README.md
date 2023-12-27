# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.github.mcollovati.vertx/vertx-vaadin-flow.svg?label=vertx-vaadin-flow)

## Description

Vertx Vaadin is an adapter library that lets you run [Vaadin](https://vaadin.com/) applications on top of [Vert.x](http://vertx.io/).
This means you can mix the simplicity and robustness of Vaadin applications with the powerful tools provided by Vert.x, such as event bus, clustering, High Availability and Fail-Over.

> [!IMPORTANT]
> Vaadin 8 is not supported anymore.

## Installation and Getting Started

Vertx-vaadin binaries are available on Maven Central and Bintray.

### Maven
```

```xml
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>
```


Snapshot are currently published on GitHub, so a [Personal Access Token](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token)
is required to download the artifacts

```xml
<repository>
	<id>vertx-vaadin-snapshots</id>
	<url>https://maven.pkg.github.com/mcollovati/vertx-vaadin</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>	
</repository>
```

In setting.xml
```xml
<server>
    <id>vertx-vaadin-snapshots</id>
    <username>your username</username>
    <password>PAT with read:packages</password>
</server>
```

## Compatibility matrix

| Vaadin version | Vert.x version | vertx-vaadin version     |Status|
|----------------|--------------|--------------------------|------|
| 23.1.1         | 4.2.3        | vertx-vaadin-flow:23.1.x |[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development)|
| 23.3           | 4.3          | vertx-vaadin-flow:23.3.x |[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development)|

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
