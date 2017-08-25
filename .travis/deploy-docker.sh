#!/bin/bash

docker login -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD" $DOCKER_REGISTRY
sbt ++$TRAVIS_SCALA_VERSION \
  "set dockerRepository := scala.sys.env.get(\"DOCKER_REGISTRY\")" \
  "set dockerUsername := scala.sys.env.get(\"DOCKER_REPOSITORY\")" \
  "set dockerUpdateLatest := ( scala.sys.env.getOrElse(\"DOCKER_PUSH_LATEST\", \"\").toLowerCase == \"true\" )" \
  "docker:publish"
