#!/bin/bash
if [ "$TRAVIS_BRANCH" == "master" ]; then
  bash gradlew artifactoryPublish --info
fi
