# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
[![Gitter](https://badges.gitter.im/vertx-vaadin/community.svg)](https://gitter.im/vertx-vaadin/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
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

| Vaadin version | Vert.x version | vertx-vaadin version      |Status|
|----------------|----------------|---------------------------|------|
| 8.x -> 8.7     | 3.5            | vertx-vaadin8:1.0.1       |![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)|
| 8.8            | 3.7            | vertx-vaadin8:2.0.0       |[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master)|
| 10             | 3.5            | vertx-vaadin-flow:10.0.0  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-10)|
| 12             | 3.5            | vertx-vaadin-flow:12.0.0  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-12)|
| 13             | 3.5            | vertx-vaadin-flow:13.0.1  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-13)|
| 14.0           | 3.7            | vertx-vaadin-flow:14.0.0  |[![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/134)|
| 14.0           | 3.8.3          | vertx-vaadin-flow:14.0.10 |[![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14)|
| 14.1           | 3.8.3          | vertx-vaadin-flow:14.1.0  |[![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master)|
| 14.2           | 3.8.3          | vertx-vaadin-flow:14.2.x  |[![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/672)|
| 14.6           | 4.0.3          | vertx-vaadin-flow:14.4.x  |[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14.2.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14.2)|
| 15             | 3.8.5          | vertx-vaadin-flow:15.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-15)|
| 16             | 3.8.5          | vertx-vaadin-flow:16.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-16)|
| 17             | 3.8.5          | vertx-vaadin-flow:17.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-17)|
| 18             | 3.8.5          | vertx-vaadin-flow:18.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-18)|
| 19             | 3.8.5          | vertx-vaadin-flow:19.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-19)|
| 20             | 4.0.3          | vertx-vaadin-flow:20.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/721)|
| 21             | 4.0.3          | vertx-vaadin-flow:21.0.x  |[![CircleCI](https://img.shields.io/badge/discontinued-inactive.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/952)|
| 23.1.1         | 4.2.3          | vertx-vaadin-flow:23.1.x  |[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/development)|

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
