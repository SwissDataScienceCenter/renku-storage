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

package models

import javax.inject.Inject
import java.sql.Timestamp
import java.util.UUID
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.SqlType
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

case class Event(uuid : UUID, event : String, created : Timestamp)

class EventRepo @Inject()(@NamedDatabase("default") protected val dbConfigProvider: DatabaseConfigProvider) {

    val dbConfig = dbConfigProvider.get[JdbcProfile]
    val db = dbConfig.db
    import dbConfig.profile.api._

    class Events(tag : Tag) extends Table[Event](tag, "EVENT_LOG") {
        def uuid    : Rep[UUID] = column[UUID]("EVENT_ID", O.PrimaryKey)
        def event   : Rep[String] = column[String]("EVENT")
        def created : Rep[Timestamp] = column[Timestamp]("CREATED")
        def * : ProvenShape[Event] = (uuid, event, created) <> (Event.tupled, Event.unapply)
    }

    object events extends TableQuery(new Events(_)) {
    }

    def insert(uuid : UUID, event : JsValue) : Future[Event] = db.run {
        (events.map(e => (e.uuid, e.event))
           returning events.map(_.created)
           into ((tuple, created) => Event(tuple._1, tuple._2, created))
        ) += (uuid, event.toString())
        //events += event).map(_ => ()
    }

    def insert(event : JsValue) : Future[Event] = insert(UUID.randomUUID(), event)
}
