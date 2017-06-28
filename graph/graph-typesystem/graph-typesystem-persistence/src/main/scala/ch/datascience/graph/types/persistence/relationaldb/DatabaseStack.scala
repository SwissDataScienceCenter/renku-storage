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

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

/**
  * Created by johann on 13/04/17.
  */
class DatabaseStack(protected val dbConfig: DatabaseConfig[JdbcProfile])
  extends JdbcProfileComponent
    with SchemasComponent
    with ImplicitsComponent
    with EntityComponent
    with StateComponent
    with TransitionComponent
    with AbstractEntityComponent
    with GraphDomainComponent
    with PropertyKeyComponent
    with NamedTypeComponent
    with EdgeLabelComponent
    with SystemPropertyKeyComponent
