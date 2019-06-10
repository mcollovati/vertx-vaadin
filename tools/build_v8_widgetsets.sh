#!/usr/bin/env bash

vaadin_releases=(8.7 8.6 8.5)
for rel in  ${vaadin_releases[@]}; do

    echo "Fetch Vaadin versions for ${rel}..."
    versions=$(curl -s "https://search.maven.org/solrsearch/select?q=g:com.vaadin+AND+a:vaadin-client+AND+v:${rel}.*&rows=10&core=gav" | jq -r '.response.docs[].v')

    for version in ${versions}; do
        echo "Deploying vertx-vaadin for ${version}"
        mvn -B -Prelease-vaadin8 -pl :vertx-vaadin8 -DskipTests -Dvertx-vaadin.release -DskipDefaultJar -Dvaadin.version=${version} clean deploy
    done
done

