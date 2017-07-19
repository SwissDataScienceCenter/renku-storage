package controllers

import javax.inject.{Inject, Singleton}

import authorization.JWTVerifierProvider
import ch.datascience.graph.elements.mutation.{GraphMutationClient, Mutation}
import ch.datascience.graph.elements.mutation.create.{CreateEdgeOperation, CreateVertexOperation}
import ch.datascience.graph.elements.new_.NewEdge
import ch.datascience.graph.elements.new_.build.NewVertexBuilder
import ch.datascience.graph.elements.persisted.PersistedVertex
import ch.datascience.graph.naming.NamespaceAndName
import ch.datascience.graph.values.StringValue
import ch.datascience.service.security.ProfileFilterAction
import ch.datascience.service.utils.ControllerWithBodyParseJson
import ch.datascience.service.models.resource.json._
import ch.datascience.service.models.storage.json._
import ch.datascience.graph.elements.mutation.log.model.json._
import ch.datascience.service.models.storage.{CreateBucketRequest, CreateFileRequest, ReadResourceRequest, WriteResourceRequest}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import clients.ResourcesManagerClient
import controllers.storageBackends.Backends
import org.apache.tinkerpop.gremlin.structure.Vertex
import persistence.graph.{GraphExecutionContextProvider, JanusGraphTraversalSourceProvider}
import persistence.reader.VertexReader
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

/**
  * Created by jeberle on 25.04.17.
  */
@Singleton
class AuthorizeController @Inject()(config: play.api.Configuration,
                                    jwtVerifier: JWTVerifierProvider,
                                    implicit val wsclient: WSClient,
                                    implicit val graphExecutionContextProvider: GraphExecutionContextProvider,
                                    implicit val janusGraphTraversalSourceProvider: JanusGraphTraversalSourceProvider,
                                    implicit val vertexReader: VertexReader,
                                    backends: Backends
                                    ) extends Controller with ControllerWithBodyParseJson with GraphTraversalComponent with RequestHelper {

  lazy val host: String = config
    .getString("resources.manager.service.host")
    .getOrElse("http://localhost:9000/api/resources/")

  lazy val mhost: String = config
    .getString("graph.mutation.service.host")
    .getOrElse("http://graph-mutation:9000/api/mutation")

  def get_property(persistedVertex: PersistedVertex, name: String) =
    persistedVertex.properties.get(NamespaceAndName(name)).flatMap(v => v.values.headOption.map(value => value.asInstanceOf[StringValue].self))

  def objectRead = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[ReadResourceRequest](ReadResourceRequestFormat)) { implicit request =>

    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val g = graphTraversalSource
    val t = g.V(Long.box(request.body.resourceId)).as("data").out("resource:stored_in").as("bucket").select[Vertex]("data", "bucket")

    graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        (for {
          data <- jmap.get("data").map(v => vertexReader.read(v))
          bucket <- jmap.get("bucket").map(v => vertexReader.read(v))
        } yield {
          for {d <- data; b <- bucket} yield {
            Some(Json.toJson(Map(
              "bucket" -> get_property(b, "resource:bucket_name").getOrElse(""),
              "name" -> get_property(d, "resource:file_name").getOrElse(""),
              "backend" -> get_property(b, "resource:bucket_backend").getOrElse("")
            )).as[JsObject])
          }
        }).getOrElse(Future(None))
      }
      else
        Future(None)
    }.flatMap(extra => {
      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra))
    }).map(ret => Ok(ret))

  }

  def objectWrite = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[WriteResourceRequest](WriteResourceRequestFormat)) { implicit request =>
    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val g = graphTraversalSource
    val t = g.V(Long.box(request.body.resourceId)).as("data").out("resource:stored_in").as("bucket").select[Vertex]("data", "bucket")

    graphExecutionContext.execute {
      if (t.hasNext) {
        import scala.collection.JavaConverters._
        val jmap: Map[String, Vertex] = t.next().asScala.toMap
        (for {
          data <- jmap.get("data").map(v => vertexReader.read(v))
          bucket <- jmap.get("bucket").map(v => vertexReader.read(v))
        } yield {
          for {d <- data; b <- bucket} yield {
            Some(Json.toJson(Map(
              "bucket" -> get_property(b, "resource:bucket_name").getOrElse(""),
              "name" -> get_property(d, "resource:file_name").getOrElse(""),
              "backend" -> get_property(b, "resource:bucket_backend").getOrElse("")
            )).as[JsObject])
          }
        }).getOrElse(Future(None))
      }
      else
        Future(None)
    }.flatMap(extra => {
      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra))
    }).map(ret => Ok(ret))
  }

  def objectCreate = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[CreateFileRequest](CreateFileRequestFormat)) { implicit request =>
    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    getVertex(request.body.bucketId).flatMap {
      case Some(vertex) =>
        if (vertex.types.contains(NamespaceAndName("resource:bucket"))) {

          val backend = get_property(vertex, "resource:bucket_backend").getOrElse("")
          val extra = Some(Json.toJson(Map(
            "bucket" -> get_property(vertex, "resource:bucket_name").getOrElse(""),
            "name" -> request.body.fileName,
            "backend" -> backend
          )).as[JsObject])
          rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra)).flatMap(res => {
            val nvertex = new NewVertexBuilder()
              .addSingleProperty("resource:file_name", StringValue(request.body.fileName))
              .addType(NamespaceAndName("resource:file"))
            val edge = NewEdge(NamespaceAndName("resource:stored_in"), Right(vertex.id), Left(0), Map())
            val mut = Mutation(Seq(CreateVertexOperation(nvertex.result()), CreateEdgeOperation(edge)))
            val gc = GraphMutationClient.makeStandaloneClient(mhost)
            gc.post(mut).map(ev => Ok(res))  //TODO: maybe take into account if the node was created or not
          })
        } else {
          Future(BadRequest("Resource is not a bucket"))
        }
      case None => Future(BadRequest("Unknown resource Id"))
    }

  }

  def bucketCreate = ProfileFilterAction(jwtVerifier.get).async(bodyParseJson[CreateBucketRequest](CreateBucketRequestFormat)) { implicit request =>
    implicit val token: String = request.headers.get("Authorization").getOrElse("")
    val rmc = new ResourcesManagerClient(host)
    val backend = request.body.backend
    val name = request.body.name
    val extra = Some(Json.toJson(Map(
        "bucket" -> name,
        "backend" -> backend
      )).as[JsObject])

      rmc.authorize(AccessRequestFormat, request.body.toAccessRequest(extra)).flatMap(res =>
        backends.getBackend(backend) match {
          case Some(back) =>
            val bid = back.createBucket(request, request.body.name)
            val vertex = new NewVertexBuilder()
              .addSingleProperty("resource:bucket_backend_id", StringValue(bid))
              .addSingleProperty("resource:bucket_name", StringValue(name))
              .addSingleProperty("resource:bucket_backend", StringValue(backend))
              .addType(NamespaceAndName("resource:bucket"))
            val mut = Mutation(Seq(CreateVertexOperation(vertex.result())))
            val gc = GraphMutationClient.makeStandaloneClient(mhost)
            gc.post(mut).flatMap(ev => gc.wait(ev.uuid).map(e => Created(Json.toJson(e))))

          case None => Future(BadRequest(s"The backend $backend is not enabled."))
        })

    }
}
