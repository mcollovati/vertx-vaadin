#!/usr/bin/env bash

_base_dir="$(dirname $(realpath $0))/.."
_newVersion=${1:?New version is missing}
_mvn="$_base_dir/mvnw -f $_base_dir/pom.xml"

$_mvn -Pflow-ui-tests versions:set -DnewVersion=${_newVersion}
