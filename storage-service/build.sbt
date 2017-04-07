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

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += filters
libraryDependencies ++= Seq(
  "io.minio" % "minio" % "3.0.3",
  cache,
  "org.pac4j" % "play-pac4j" % "3.0.0-RC2",
  "org.pac4j" % "pac4j-oidc" % "2.0.0-RC2",
  "org.pac4j" % "pac4j-jwt" % "2.0.0-RC2",
  "org.pac4j" % "pac4j-http" % "2.0.0-RC2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

resolvers ++= Seq(
  DefaultMavenRepository,
  "SDSC Snapshots" at "https://internal.datascience.ch:8081/nexus/content/repositories/snapshots/",
  Resolver.mavenLocal
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ch.datascience.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ch.datascience.binders._"
