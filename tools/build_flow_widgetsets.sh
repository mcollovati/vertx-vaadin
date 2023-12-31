#!/usr/bin/env bash

#######
# Usage $0 <action> <kind>
#
#
# action : maven goal to execute, defaults to 'package'
# kind   : artifact type (snapshot, release), defaults to 'release'
#
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

echo "Search for existing classifiers..."

__repo_name=vertx-vaadin
if [[ "${_kind}" = "snapshot" ]]; then
  __repo_name="${__repo_name}-snapshots"
fi
__base_url="https://repo.repsy.io/mvn/mcollovati/${__repo_name}/com/github/mcollovati/vertx/vaadin-flow-sockjs/${_current_version}"
if [[ "$(curl -s -o /dev/null -w '%{http_code}' ${__base_url}/maven-metadata.xml)" = "200" ]]; then
  __existing_classifiers=$(curl -s ${__base_url}/maven-metadata.xml \
      | grep "<classifier>vaadin-" | sed -E 's/^.*<classifier>vaadin-(.*)<\/classifier>.*/\1/g' | sort -r -t '.' || echo '')
else
  # Extract existing versions from directory listing
  __pattern='href="vaadin-flow-sockjs-23.4.0-alpha1-vaadin-([^"]+)\.jar\"'
  __existing_classifiers=$(curl -s ${__base_url}/ | sed -E -e "/${__pattern}/!d" -e "s/.*${__pattern}.*/\1/g" || echo '')
fi

echo "Existing classifiers for version ${_current_version} ===> ${__existing_classifiers}"
echo

declare -A _existing_versions
for v in ${__existing_classifiers}; do
  _existing_versions[$v]=$v
done

#if [[ ${#_existing_versions[@]} -eq 0 ]]; then
#  echo "deploy base version"
#    $_mvn -B -ntp -pl :vaadin-flow-sockjs -Dvertx-vaadin.release=${_kind} -DskipTests -Dvaadin.platform.version=${version} -Dvaadin.flow.version=${flow_client_version} $_mvn_target ${DEPLOY_OPTS}
#    _last_built=${flow_client_version}
#fi

_last_built=""
for version in "${versions[@]}"; do

  if [[ ${_existing_versions[$version]} ]]; then
    echo "Classifier vaadin-${version} already exists for version ${_current_version}"
  else
    echo "Building classifier vaadin-${version} for version ${_current_version}"
    echo "Find flow-client version for vaadin ${version}..."
    flow_client_version=$($_mvn -N -ntp -Pfind-flow-client-version -q dependency:list -Dvaadin.platform.version=${version} \
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

    $_mvn -B --fail-never -ntp -pl :vaadin-flow-sockjs -Dvertx-vaadin.release=${_kind} -DskipTests -Dvaadin.platform.version=${version} -Dvaadin.flow.version=${flow_client_version} $_mvn_target ${DEPLOY_OPTS}
    _last_built=${flow_client_version}
  fi

done

