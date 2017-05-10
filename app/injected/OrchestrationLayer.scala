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

package injected

import javax.inject.Inject

import ch.datascience.graph.types.persistence.orchestration.OrchestrationStack
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext

/**
  * Created by johann on 13/04/17.
  */
class OrchestrationLayer @Inject()(
                                    @NamedDatabase("sqldb") protected val dbConfigProvider : DatabaseConfigProvider,
                                    override protected val dal: DatabaseLayer,
                                    override protected val gal: GraphLayer,
                                    override protected val gdb: GraphRunner
                                  )
  extends OrchestrationStack(
    ec = play.api.libs.concurrent.Execution.defaultContext,
    dbConfig = dbConfigProvider.get,
    dal = dal,
    gdb = gdb,
    gal = gal
  )
