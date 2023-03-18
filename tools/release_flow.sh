#!/usr/bin/env bash

_base_dir="$(dirname $(realpath $0))/.."
_newVersion=${1:?New version is missing}
_mvn="$_base_dir/mvnw -f $_base_dir/pom.xml"

$_mvn -Prelease-flow -pl :vaadin-flow-sockjs versions:set -DnewVersion=${_newVersion}
$_mvn -Prelease-flow -pl :vertx-vaadin-flow versions:set -DnewVersion=${_newVersion}
$_mvn -Prelease-flow -pl :vertx-vaadin-flow-jandex versions:set -DnewVersion=${_newVersion}
$_mvn -Prelease-flow -pl :vertx-vaadin-quarkus-extension-parent versions:set -DnewVersion=${_newVersion}
$_mvn -Prelease-flow,flow-ui-tests -pl :vertx-vaadin-test versions:set -DnewVersion=${_newVersion}
