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

//package ch.datascience.typesystem.orchestration
//
//import ch.datascience.typesystem.graphdb.GraphAccessLayer
//import ch.datascience.typesystem.model.table.DataAccessLayer
//import org.janusgraph.core.JanusGraph
//import slick.basic.DatabaseConfig
//import slick.jdbc.JdbcBackend.Database
//import slick.jdbc.JdbcProfile
//
///**
//  * Created by johann on 04/04/17.
//  */
//class Orchestrator(val db: Database, val dal: DataAccessLayer, val gal: GraphAccessLayer) extends DatabaseComponent with GraphComponent with GraphDomainOrchestrator with PropertyKeyOrchestrator  {
//}
//
//object Orchestrator {
//
//  def apply(dbConfig: DatabaseConfig[_ <: JdbcProfile], graph: JanusGraph): Orchestrator = {
//    val db = Database.forConfig("", config = dbConfig.config)
//    val dal = new DataAccessLayer(dbConfig.profile)
//
//    val gal = new GraphAccessLayer(graph)
//
//    new Orchestrator(db, dal, gal)
//  }
//
//}
