#!/usr/bin/env bash

vaadin_releases=(10)
for rel in  ${vaadin_releases[@]}; do

    echo "Fetch Vaadin versions for ${rel}..."
    versions=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.vaadin+AND+a:vaadin-core+AND+v:${rel}.*&rows=10&core=gav" | jq -r '.response.docs[].v')

    for version in ${versions}; do

        echo "Find flow-client version for vaadin ${version}..."
        flow_client_version=$(mvn -Pfind-flow-client-version -q dependency:list -Dvaadin.platform.version=${version} \
            -DincludeArtifactIds=flow-client -DoutputFile=target/flow-client.version && \
            cat target/flow-client.version | grep 'com.vaadin:flow-client' | cut -d ':' -f 4)

        echo "Deploying vaadin-flow-sockjs for vaadin ${version}, flow client ${flow_client_version}"
        mvn -B -Prelease-flow -pl :vaadin-flow-sockjs -Dvertx-vaadin.release -DskipTests -Dvaadin.platform.version=${version} -Dvaadin.flow.version=${flow_client_version} clean deploy
    done
done

