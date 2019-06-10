# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
![Bintray](https://img.shields.io/bintray/v/mcollovati/maven-repo/vertx-vaadin.svg?label=vertx-vaadin8&colorB=blue)
![Bintray](https://img.shields.io/bintray/v/mcollovati/maven-repo/vertx-vaadin-flow.svg?label=vertx-vaadin-flow&colorB=blue)


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
  <version>0.4.0</version>
</dependency>
```

```
<dependency>
  <groupId>com.github.mcollovati.vertx</groupId>
  <artifactId>vertx-vaadin-flow</artifactId>
  <version>0.1.0</version>
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

|Vaadin version|vertx-vaadin version|Status|
|--------------|--------------------|------|
|8.x|vertx-vaadin8:1.0.0|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/master)|
|10|vertx-vaadin-flow:10.0.0|[![CircleCI](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-10.svg?style=svg)](https://circleci.com/gh/mcollovati/vertx-vaadin/tree/vaadin-10)|

## Documentation

See [vertx-vaadin-8](vertx-vaadin-8-parent/vertx-vaadin8)  or [vertx-vaadin-flow](vertx-vaadin-flow-parent/vertx-vaadin-flow) module for more information.

## Demo and samples

A public demo based on Vaadin 8 archetype application example is published on heroku and is available [here](http://vertx-vaadin-example.herokuapp.com/)

Bookstore demo on Vaadin Flow is published on heroku and is available [here](https://vertx-vaadin10-example.herokuapp.com/)

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