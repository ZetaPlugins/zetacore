#!/bin/bash

VERSION=$(xmllint --xpath "/*[local-name()='project']/*[local-name()='version']/text()" plugin-test/pom.xml)
echo "Building version $VERSION"

mvn clean install

mkdir -p server/plugins

cp plugin-test/target/plugin-test-$VERSION.jar server/plugins/