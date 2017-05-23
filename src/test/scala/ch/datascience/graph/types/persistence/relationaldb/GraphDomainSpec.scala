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

package ch.datascience.graph.types.persistence.relationaldb

import java.util.UUID

import ch.datascience.graph.types.persistence.model.GraphDomain
import ch.datascience.graph.types.persistence.{AsyncUnitSpec, DatabaseSetup}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by johann on 13/04/17.
  */
class GraphDomainSpec extends AsyncUnitSpec with DatabaseSetup with BeforeAndAfterEach {

  import profile.api._

  behavior of "GraphDomains table"

  it should "be empty" in {
    val f = db.run(dal.graphDomains.result)
    f map { list => list shouldBe empty }
  }

  it should "allow to add a graph domain" in {
    val graphDomain = GraphDomain(UUID.randomUUID(), "foo")
    val f = db.run( dal.graphDomains add graphDomain )
    f map { unit => unit shouldBe () }
  }

  it should "allow to add a graph domain and get it back" in {
    val graphDomain = GraphDomain(UUID.randomUUID(), "foo")
    val insert: DBIO[Unit] = dal.graphDomains add graphDomain
    val select: DBIO[Option[GraphDomain]] = dal.graphDomains.findByNamespace("foo").result.headOption
    val f = db.run(insert andThen select)
    f map { opt => opt shouldBe Some(graphDomain) }
  }

  override protected def afterEach(): Unit = {
    try super.afterEach()
    finally {
      val dropAll: DBIO[Int] = dal.graphDomains.delete
      val run = db.run(dropAll)
      Await.result(run, Duration.Inf)
    }
  }

}
