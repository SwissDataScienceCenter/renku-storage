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

package ch.datascience.graph.elements.mutation.log

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by johann on 07/06/17.
  */
//trait DatabaseConfigComponent[Profile <: JdbcProfile] {
//
//  protected val dbConfig: DatabaseConfig[Profile]
//
//  protected final lazy val profile: Profile = dbConfig.profile
//
//  // Would be nice but is broken :/
//    protected final def db: Profile#Backend#Database = dbConfig.db
////  protected final def db: Profile#Backend#Database = profile.api.Database.forConfig("", config = dbConfig.config)
//
//}
