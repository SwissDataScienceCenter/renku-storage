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

package models.persistence

import javax.inject.{ Inject, Singleton }

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

/**
 * Created by johann on 13/04/17.
 */

@Singleton
class DatabaseLayer @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)
  extends DatabaseStack(
    dbConfig = dbConfigProvider.get
  ) {
  import profile.api._

  lazy val logger: Logger = Logger( "application.DatabaseLayer" )

  val tables = List(
    TableQuery[Repositories],
    TableQuery[FileObjects],
    TableQuery[FileObjectRepositories],
    TableQuery[Events]
  )

  val init = db.run( MTable.getTables ).flatMap( v => {
    val names = v.map( mt => mt.name.name )
    val createIfNotExist = tables.filter( table =>
      !names.contains( table.baseTableRow.tableName ) ).map( _.schema.create )
    db.run( DBIO.sequence( createIfNotExist ) )
  } )
  Await.result( init, Duration.Inf )

  init.onFailure {
    case NonFatal( t ) =>
      logger.error( t.getMessage )
  }
}
