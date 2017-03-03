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

organization := "ch.datascience"
version := "0.0.1"
scalaVersion := "2.11.8"

resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.mavenLocal,
  "Local Maven Repository" at "" + Path.userHome.asFile.toURI.toURL + "/.m2/repository"
)


val janusgraph_version = "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.janusgraph" % "janusgraph-core" % janusgraph_version,
  "junit" % "junit" % "4.12" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test
)