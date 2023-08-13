#!/usr/bin/env bash

#######
# Usage $0 <action> <kind>
#
#
# action : maven goal to execute, defaults to 'package'
# kind   : artifact type (snapshot, release), defaults to 'release'
#
# cat maven-metadata.xml | grep "<version>" | sed 's/^.*<version>\([^<]*\)<.*/\1/g' | sort -r
# curl -s https://repo1.maven.org/maven2/com/vaadin/vaadin-core/maven-metadata.xml | grep "<version>23\..*</version>" | sed -E 's/^.*<version>(.*)<\/version>.*/\1/g' | sort -r -t '.'
#######

_base_dir="$(dirname $(realpath $0))/.."
_action=${1:-package}
_kind=${2:-release}
_mvn="$_base_dir/mvnw -f $_base_dir/pom.xml"

_current_version=$($_mvn -Prelease-flow -pl :vaadin-flow-sockjs help:evaluate -q -Dexpression='project.version' -DforceStdout=true)

declare -ga versions
function get_vaadin_versions() {
    #local __result=$1
    local vaadin_platform_full=$($_mvn -N help:evaluate -q -Dexpression='vaadin.platform.version' -DforceStdout=true)
    local vaadin_platform=$(echo $vaadin_platform_full | cut -d '.' -f 1,2)
    local __versions
    echo "Fetch Vaadin versions for ${vaadin_platform}..."
    local __metadata_base_url
    case $vaadin_platform_full in
        *-SNAPSHOT|*.alpha*|*.beta*|*.rc*)
        __metadata_base_url=https://maven.vaadin.com/vaadin-prereleases
        ;;
        *)
        __metadata_base_url=https://repo1.maven.org/maven2
        ;;
    esac
    __versions=$(curl -s ${__metadata_base_url}/com/vaadin/vaadin-core/maven-metadata.xml | \
      grep "<version>${vaadin_platform}\..*</version>" | sed -E 's/^.*<version>(.*)<\/version>.*/\1/g' | \
      sort -V -r | head -n 10
    )
    echo "Found Vaadin versions:"
    echo "${__versions}"
    echo
    for v in ${__versions}; do
      versions+=("$v")
    done
    #eval $__result="(${__versions}'"
}
get_vaadin_versions #"versions"

###versions=("${versions[@]:9:3}")
echo "Search for existing classifiers..."
declare -A _existing_versions
for version in "${versions[@]}"; do

  flag=$(
    mvn -q dependency:get -Dartifact=com.github.mcollovati.vertx:vaadin-flow-sockjs:${_current_version}:jar:vaadin-${version} \
      -DremoteRepositories=github-vertx-vaadin::::https://maven.pkg.github.com/mcollovati/vertx-vaadin/ -Dtransitive=false 2>&1 >/dev/null && \
      echo 1 || echo 0
  )
  echo "  --> Searching classifier vaadin-${version} for version ${_current_version} ===> ${flag}"
  if [[ "${flag}" = "1" ]]; then
    _existing_versions[$version]=$version
  fi
done

echo "Existing classifiers for ${_current_version}:"
echo "${_existing_versions[@]}"
echo

_last_built=""
for version in "${versions[@]}"; do

  if [[ ${_existing_versions[$version]} ]]; then
    echo "Classifier vaadin-${version} already exists for version ${_current_version}"
  else
    echo "Building classifier vaadin-${version} for version ${_current_version}"
    echo "Find flow-client version for vaadin ${version}..."
    flow_client_version=$($_mvn -Pfind-flow-client-version -q dependency:list -Dvaadin.platform.version=${version} \
        -DincludeArtifactIds=flow-client -DoutputFile=$_base_dir/target/flow-client.version && \
        cat $_base_dir/target/flow-client.version | grep 'com.vaadin:flow-client' | cut -d ':' -f 4)

    _mvn_target="$_action"
    if [[ "${_last_built}" = "${flow_client_version}" ]]; then
        echo "Deploy already built vaadin-flow-sockjs based on ${flow_client_version} for vaadin ${version}"
        _mvn_target="-DskipBuild=true $_action"
    else
        echo "Deploying vaadin-flow-sockjs for vaadin ${version}, flow client ${flow_client_version}"
        _mvn_target="clean $_action"
    fi

    $_mvn -B --fail-never -ntp -Prelease-flow -pl :vaadin-flow-sockjs -Dvertx-vaadin.release=${_kind} -DskipTests -Dvaadin.platform.version=${version} -Dvaadin.flow.version=${flow_client_version} $_mvn_target ${DEPLOY_OPTS}
    _last_built=${flow_client_version}
  fi

done

