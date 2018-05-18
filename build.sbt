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
version := "1.0.1-SNAPSHOT"
scalaVersion := "2.11.8"
name := "renku-storage"

lazy val root = (project in file("."))
  .enablePlugins(
    PlayScala
  )

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val renku_version = "0.2.0-SNAPSHOT"
libraryDependencies += "ch.datascience" %% "renku-commons" % renku_version

libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.8"

lazy val play_slick_version = "2.1.1"
lazy val postgresql_version = "42.0.0"

libraryDependencies += filters
libraryDependencies += "com.typesafe.play" %% "play-slick" % play_slick_version
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % play_slick_version
libraryDependencies += "org.postgresql" % "postgresql" % postgresql_version
libraryDependencies += "com.h2database" % "h2" % "1.4.196"

libraryDependencies += "io.minio" % "minio" % "3.0.4"
libraryDependencies += "org.javaswift" % "joss" % "0.9.7"
libraryDependencies += "com.microsoft.azure" % "azure-storage" % "6.1.0"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "4.9.2.201712150930-r"
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % "4.9.2.201712150930-r"

libraryDependencies += cache
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.8.47" % Test

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

import com.typesafe.sbt.packager.docker._
lazy val newEntrypoint = "bin/docker-entrypoint.sh"
mappings in Docker += (file(".") / "docker-entrypoint.sh") -> ((defaultLinuxInstallLocation in Docker).value + s"/$newEntrypoint")

// Source code formatting
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

val preferences =
  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference( AlignArguments,                               true  )
    .setPreference( AlignParameters,                              true  )
    .setPreference( AlignSingleLineCaseStatements,                true  )
    .setPreference( AlignSingleLineCaseStatements.MaxArrowIndent, 40    )
    .setPreference( CompactControlReadability,                    true  )
    .setPreference( CompactStringConcatenation,                   false )
    .setPreference( DanglingCloseParenthesis,                     Force )
    .setPreference( DoubleIndentConstructorArguments,             true  )
    .setPreference( DoubleIndentMethodDeclaration,                true  )
    .setPreference( FirstArgumentOnNewline,                       Force )
    .setPreference( FirstParameterOnNewline,                      Force )
    .setPreference( FormatXml,                                    true  )
    .setPreference( IndentPackageBlocks,                          true  )
    .setPreference( IndentSpaces,                                 2     )
    .setPreference( IndentWithTabs,                               false )
    .setPreference( MultilineScaladocCommentsStartOnFirstLine,    false )
    .setPreference( NewlineAtEndOfFile,                           true  )
    .setPreference( PlaceScaladocAsterisksBeneathSecondAsterisk,  false )
    .setPreference( PreserveSpaceBeforeArguments,                 false )
    .setPreference( RewriteArrowSymbols,                          false )
    .setPreference( SpaceBeforeColon,                             false )
    .setPreference( SpaceBeforeContextColon,                      true  )
    .setPreference( SpaceInsideBrackets,                          false )
    .setPreference( SpaceInsideParentheses,                       true  )
    .setPreference( SpacesAroundMultiImports,                     true  )
    .setPreference( SpacesWithinPatternBinders,                   false )

SbtScalariform.scalariformSettings ++ Seq(preferences)
