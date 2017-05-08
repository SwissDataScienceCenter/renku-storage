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
name := "graph-all"
version := "0.0.1-SNAPSHOT"
scalaVersion := "2.11.8"

// This project contains nothing to package, like pure POM maven project
packagedArtifacts := Map.empty

lazy val core = Project(
  id   = "graph-core",
  base = file("graph-core")
).settings(
  commonSettings,
  scriptsSettings
)

lazy val typesystem = Project(
  id   = "graph-typesystem",
  base = file("graph-typesystem")
).settings(
  commonSettings,
  scriptsSettings
).dependsOn(core)

lazy val initProjects = taskKey[Unit]("Execute the init script")
lazy val updateProjects = taskKey[Unit]("Execute the pull script")

lazy val scriptsSettings = Seq(
  initProjects := {
    println(s"Calling: scripts/init.sh ${name.value} ${baseDirectory.value}")
    s"scripts/init.sh ${name.value} ${baseDirectory.value}" !
  }
, updateProjects := {
    println(s"Calling: scripts/pull.sh ${name.value} ${baseDirectory.value}")
    s"scripts/pull.sh ${name.value} ${baseDirectory.value}" !
  }
, updateProjects := (updateProjects dependsOn initProjects).value
, compile in Compile := ((compile in Compile) dependsOn initProjects).value
)

lazy val commonSettings = Seq(
  organization := "ch.datascience"
, scalaVersion := "2.11.8"
)
