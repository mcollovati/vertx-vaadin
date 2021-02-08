#!/usr/bin/env bash

_base_dir="$(dirname $(realpath $0))/.."
_action=${1:-package}
_kind=${2:-release}
_mvn="$_base_dir/mvnw -f $_base_dir/pom.xml"


function get_vaadin_versions() {
    local __result=$1
    local vaadin_platform_full=$($_mvn -N help:evaluate -q -Dexpression='vaadin.platform.version' -DforceStdout=true)
    local vaadin_platform=$(echo $vaadin_platform_full | cut -d '.' -f 1)
    local __versions
    echo "Fetch Vaadin versions for ${vaadin_platform}..."
    case $vaadin_platform_full in
        *-SNAPSHOT)
        __versions=$(curl -s https://maven.vaadin.com/vaadin-prereleases/com/vaadin/vaadin-core/maven-metadata.xml | grep "<version>${vaadin_platform}\..*</version>" | sed -E 's/^.*<version>(.*)<\/version>.*/\1/g')
        ;;
        *)
        __versions=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.vaadin+AND+a:vaadin-core+AND+v:${vaadin_platform}.*&rows=10&core=gav" | jq -r '.response.docs[].v')
        ;;
    esac
    echo "Found versions: ${__versions}"
    eval $__result="'${__versions}'"
}
#vaadin_releases=$($_mvn -N help:evaluate -q -Dexpression='vaadin.platform.version' -DforceStdout=true | cut -d '.' -f 1)
#for rel in  ${vaadin_releases[@]}; do

#    echo "Fetch Vaadin versions for ${rel}..."
#    versions=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.vaadin+AND+a:vaadin-core+AND+v:${rel}.*&rows=10&core=gav" | jq -r '.response.docs[].v')
    get_vaadin_versions "versions"

    _last_built=""
    for version in ${versions}; do

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

        $_mvn -B --fail-never -Prelease-flow -pl :vaadin-flow-sockjs -Dvertx-vaadin.release.gpg -Dvertx-vaadin.${_kind} -DskipTests -Dvaadin.platform.version=${version} -Dvaadin.flow.version=${flow_client_version} $_mvn_target
        _last_built=${flow_client_version}
    done
#done


