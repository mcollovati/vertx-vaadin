# Vertx Vaadin

## Status

![License](https://img.shields.io/github/license/mcollovati/vertx-vaadin.svg)
[![Build Status](https://travis-ci.org/mcollovati/vertx-vaadin.svg?branch=master)](https://travis-ci.org/mcollovati/vertx-vaadin)
[ ![Download](https://api.bintray.com/packages/mcollovati/maven-repo/vertx-vaadin/images/download.svg) ](https://bintray.com/mcollovati/maven-repo/vertx-vaadin/_latestVersion)

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
  <version>0.3.0</version>
</dependency>
```

Optional:
```
<repository>
	<id>jcenter</id>
	<url>http://jcenter.bintray.com</url>
</repository>
```


### Gradle

```
compile 'com.github.mcollovati.vertx:vertx-vaadin8:0.3.0'
```

Optional:
```
repositories {
	jcenter()
}
```


## Documentation

See [vertx-vaadin](vertx-vaadin) module for more information.

## Demo and samples

A public demo based on Vaadin archetype application example is published on heroku and is available [here](http://vertx-vaadin-example.herokuapp.com/)

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