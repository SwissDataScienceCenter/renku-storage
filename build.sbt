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
scalaVersion := "2.12.6"
name := "renku-storage"

lazy val root = (project in file("."))
  .enablePlugins(
    PlayScala
  )

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += filters

lazy val renku_version = "0.2.0-SNAPSHOT"
libraryDependencies += "ch.datascience" %% "renku-commons" % renku_version

lazy val logback_encoder_version = "5.1"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % logback_encoder_version

lazy val play_slick_version = "3.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick" % play_slick_version
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % play_slick_version

lazy val postgresql_version = "42.2.2"
libraryDependencies += "org.postgresql" % "postgresql" % postgresql_version

lazy val h2_version = "1.4.197"
libraryDependencies += "com.h2database" % "h2" % h2_version

lazy val minio_version = "4.0.0"
libraryDependencies += "io.minio" % "minio" % minio_version

lazy val joss_version = "0.10.2"
libraryDependencies += "org.javaswift" % "joss" % joss_version

lazy val azure_storage_version = "7.0.0"
libraryDependencies += "com.microsoft.azure" % "azure-storage" % azure_storage_version

lazy val jgit_version = "4.11.0.201803080745-r"
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % jgit_version
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % jgit_version

lazy val scalatest_plus_play_version = "3.1.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % scalatest_plus_play_version % Test

lazy val mockito_version = "2.18.3"
libraryDependencies += "org.mockito" % "mockito-core" % mockito_version % Test

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
lazy val newEntrypoint = "bin/docker-entrypoint.sh"
mappings in Docker += (file(".") / "docker-entrypoint.sh") -> ((defaultLinuxInstallLocation in Docker).value + s"/$newEntrypoint")

// Source code formatting
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

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
