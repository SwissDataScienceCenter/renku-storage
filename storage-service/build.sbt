/*
 * Copyright 2017 - Swiss Data Science Center (SDSC)
 * A partnership between École Polytechnique Fédérale de Lausanne (EPFL) and
 * Eidgenössische Technische Hochschule Zürich (ETHZ).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

name := """storage-service"""
organization := "ch.datascience"

version := "1.0-SNAPSHOT"

lazy val root = Project(
  id   = "storage-service",
  base = file(".")
).dependsOn(
  core,
  mutationClient,
  serviceCommons
).enablePlugins(PlayScala)


lazy val core = RootProject(file("../graph-core"))
lazy val mutationClient = RootProject(file("../graph-mutation-client"))
lazy val serviceCommons = RootProject(file("../service-commons"))

scalaVersion := "2.11.8"
lazy val janusgraph_version = "0.1.0"

libraryDependencies += filters
libraryDependencies ++= Seq(
  "org.janusgraph" % "janusgraph-cassandra" % janusgraph_version, //% Runtime
  "io.minio" % "minio" % "3.0.3",
  "org.javaswift" % "joss" % "0.9.7",
  cache,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.mavenLocal
)

import com.typesafe.sbt.packager.docker._

dockerBaseImage := "openjdk:8-jre-alpine"
//dockerBaseImage := "openjdk:8-jre"

dockerCommands ~= { cmds => cmds.head +: ExecCmd("RUN", "apk", "add", "--no-cache", "bash") +: cmds.tail }
// Replace entry point
dockerCommands ~= { cmds =>
  cmds.map {
    case ExecCmd("ENTRYPOINT", args@_*) => ExecCmd("ENTRYPOINT", args ++ Seq("-Dconfig.resource=application.docker.conf"): _*)
    case cmd => cmd
  }
}