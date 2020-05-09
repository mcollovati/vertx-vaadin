# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.github.mcollovati.vertx/vertx-vaadin8.svg?label=vertx-vaadin8)
![Maven Central](https://img.shields.io/maven-central/v/com.github.mcollovati.vertx/vertx-vaadin-flow.svg?label=vertx-vaadin-flow)


## Description

Vertx Vaadin is an adapter library that lets you run [Vaadin](https://vaadin.com/) applications on top of [Vert.x](http://vertx.io/).
This means you can mix the simplicity and robustness of Vaadin applications with the powerful tools provided by Vert.x, such as event bus, clustering, High Availability and Fail-Over.

## Installation and Getting Started

Vertx-vaadin binaries are available on Maven Central and Bintray.

### Maven

```
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin8</artifactId>
  <version>${vertx-vaadin8.version}</version>
</dependency>
```

```
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>${vertx-vaadin-flow.version}</version>
</dependency>
```


For snapshots:
```
<repository>
	<id>oss-jfrog-snapshots</id>
	<url>https://oss.jfrog.org/artifactory/oss-snapshot-local</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>	
</repository>
```


### Gradle

```
compile 'com.github.mcollovati.vertx:vertx-vaadin8:0.4.0'
```

For snapshots:
```
repositories {
	maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}
```

## Compatibility matrix

|Vaadin version|Vert.x version|vertx-vaadin version|Status|
|--------------|--------------|--------------------|------|
|8.x -> 8.7|3.5|vertx-vaadin8:1.0.1|![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)|
|8.8|3.7|vertx-vaadin8:2.0.0|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master)|
|10|3.5|vertx-vaadin-flow:10.0.0|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-10.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-10)|
|12|3.5|vertx-vaadin-flow:12.0.0|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-12.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-12)|
|13|3.5|vertx-vaadin-flow:13.0.1|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-13.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-13)|
|14.0|3.7|vertx-vaadin-flow:14.0.0|[![CircleCI](https://img.shields.io/badge/stable-green.svg?style=for-the-badge)](https://circleci.com/gh/mcollovati/vertx-vaadin/134)|
|14.0|3.8.3|vertx-vaadin-flow:14.0.10+|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14)|
|14.1|3.8.3|vertx-vaadin-flow:14.1.x|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master)|
|14.2|3.8.3|vertx-vaadin-flow:14.2.x|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14.2.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-14.2)|
|15|3.8.5|vertx-vaadin-flow:15.0.0-SNAPSHOT|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-15.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-15)|

## Documentation

See [vertx-vaadin-8](vertx-vaadin-8-parent/vertx-vaadin8)  or [vertx-vaadin-flow](vertx-vaadin-flow-parent/vertx-vaadin-flow) module for more information.

## Demo and samples

A public demo based on Vaadin 8 archetype application example is published on heroku and is available [here](http://vertx-vaadin-example.herokuapp.com/)

Bookstore demo on Vaadin Flow is published on heroku and is available at the following urls:

* [vertx-vaadin-flow:14](https://vertx-vaadin14-example.herokuapp.com/)
* [vertx-vaadin-flow:15](https://vertx-vaadin15-example.herokuapp.com/)

Source code and other samples can be found on [vaadin-vertx-samples](https://github.com/mcollovati/vaadin-vertx-samples) repository. 

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

Thanks to [David Sowerby](https://github.com/davidsowerby) (author of [Krail framework](https://github.com/davidsowerby/krail)) for all his precious technical and non technical support.
