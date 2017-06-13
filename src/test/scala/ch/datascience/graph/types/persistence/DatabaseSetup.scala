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

package ch.datascience.graph.types.persistence

import ch.datascience.graph.types.persistence.relationaldb.DatabaseStack
import org.scalatest.{BeforeAndAfterAll, Suite}
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by johann on 13/04/17.
  */
//trait DatabaseSetup extends BeforeAndAfterAll with DatabaseConfigComponent[JdbcProfile] { this : Suite =>
trait DatabaseSetup extends BeforeAndAfterAll with HasDatabaseConfig[JdbcProfile] { this : Suite =>

  import profile.api._

  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("db-test")

//  val profile: JdbcProfile = dbConfig.profile
//
//  import profile.api._
//  val db: Database = Database.forConfig("", config = dbConfig.config)
//
//  class DatabaseStackTest(val profile: JdbcProfile) extends DatabaseStack
//
//  val dal = new DatabaseStackTest(profile)

  val dal = new DatabaseStack(dbConfig)

  override protected def beforeAll(): Unit = {
    val createSchemas: DBIO[Unit] = dal.schemas.map(_.asInstanceOf[profile.SchemaDescription]).reduce((x, y) => x ++ y).create
    val run = db.run(createSchemas)
    Await.result(run, Duration.Inf)

    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    try super.afterAll()
    finally {
      val deleteSchemas: DBIO[Unit] = dal.schemas.map(_.asInstanceOf[profile.SchemaDescription]).reduce((x,y) => x ++ y).drop
      val run = db.run(deleteSchemas)
      Await.result(run, Duration.Inf)
    }
  }

}
