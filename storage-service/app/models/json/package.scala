package models

import ch.datascience.graph.elements.mutation.Mutation
import ch.datascience.graph.elements.mutation.json.MutationFormat
import ch.datascience.graph.elements.mutation.log.model.EventStatus
import ch.datascience.graph.elements.mutation.log.model.json.EventStatusFormat
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.elements.persisted.json.PersistedVertexFormat
import play.api.libs.json._

/**
  * Created by johann on 25/04/17.
  */
package object json {


  implicit lazy val mutationFormat: Format[Mutation] = MutationFormat
  implicit lazy val vertexFormat: Format[PersistedVertex] = PersistedVertexFormat
  implicit lazy val eventStatusFormat: Format[EventStatus] = EventStatusFormat
  implicit lazy val createBucketRequestReads: Reads[CreateBucketRequest] = ResourceRequestMappers.createBucketRequestReads
  implicit lazy val readResourceRequestReads: Reads[ReadResourceRequest] = ResourceRequestMappers.readResourceRequestReads
  implicit lazy val writeResourceRequestWrites: Writes[WriteResourceRequest] = ResourceRequestMappers.writeResourceRequestWrites
  implicit lazy val createBucketRequestWrites: Writes[CreateBucketRequest] = ResourceRequestMappers.createBucketRequestWrites
  implicit lazy val readResourceRequestWrites: Writes[ReadResourceRequest] = ResourceRequestMappers.readResourceRequestWrites
  implicit lazy val writeResourceRequestReads: Reads[WriteResourceRequest] = ResourceRequestMappers.writeResourceRequestReads
}
