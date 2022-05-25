#!/usr/bin/env bash

_base_dir="$(dirname $(realpath $0))/.."
_action=${1:-package}
_kind=${2:-release}
_mvn="$_base_dir/mvnw -f $_base_dir/pom.xml"

_current_version=$($_mvn -Prelease-flow -pl :vaadin-flow-sockjs help:evaluate -q -Dexpression='project.version' -DforceStdout=true)

function get_vaadin_versions() {
    local __result=$1
    local vaadin_platform_full=$($_mvn -N help:evaluate -q -Dexpression='vaadin.platform.version' -DforceStdout=true)
    local vaadin_platform=$(echo $vaadin_platform_full | cut -d '.' -f 1)
    local __versions
    echo "Fetch Vaadin versions for ${vaadin_platform}..."
    case $vaadin_platform_full in
        *-SNAPSHOT|*.alpha*|*.beta*|*.rc*)
        __versions=$(curl -s https://maven.vaadin.com/vaadin-prereleases/com/vaadin/vaadin-core/maven-metadata.xml | grep "<version>${vaadin_platform}\..*</version>" | sed -E 's/^.*<version>(.*)<\/version>.*/\1/g')
        ;;
        *)
        __versions=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.vaadin+AND+a:vaadin-core+AND+v:${vaadin_platform}.*&rows=10&core=gav" | jq -r '.response.docs[].v')
        ;;
    esac
    echo "Found Vaadin versions:"
    echo "${__versions}"
    echo
    eval $__result="'${__versions}'"
}
get_vaadin_versions "versions"

## Fetch existing classifiers
URL="https://mcollovati.jfrog.io/artifactory/api/search/gavc?g=com.github.mcollovati.vertx&a=vaadin-flow-sockjs&c=vaadin-*&v=${_current_version}&repos=vertx-vaadin-releases"
JQ_FILTER='.results[].uri | (match("^.*/vaadin-flow-sockjs-'${_current_version}'-vaadin-(?<version>.*)\\.jar$") | .captures[].string )'

__classifiers=$(curl -s "${URL}" | jq --raw-output "${JQ_FILTER}" | sort -r)
echo "Existing classifiers for ${_current_version}:"
echo "${__classifiers[@]}"
echo
declare -A _existing_versions
for classifier in ${__classifiers}; do
  _existing_versions[${classifier}]=1
done


_last_built=""
for version in ${versions}; do

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

