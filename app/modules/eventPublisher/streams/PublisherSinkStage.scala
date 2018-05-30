package modules.eventPublisher.streams

import java.util.Properties

import akka.Done
import akka.stream._
import akka.stream.stage._
import models.Event
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import play.api.Configuration
import play.api.libs.json.{ Json, OFormat }

import scala.concurrent.{ Future, Promise }

class PublisherSinkStage(
    protected val config: Configuration
) extends GraphStageWithMaterializedValue[SinkShape[Event], Future[Done]] {

  implicit lazy val EventFormat: OFormat[Event] = Event.format

  val topic: String = config.getString( "events.push_to" ).getOrElse( "events" )

  val in: Inlet[Event] = Inlet[Event]( "events.in" )

  def shape: SinkShape[Event] = SinkShape( in )

  def createLogicAndMaterializedValue( inheritedAttributes: Attributes ): ( GraphStageLogic, Future[Done] ) = {
    val promise = Promise[Done]()
    val logic = new GraphStageLogic( shape ) with StageLogging {
      private val kafkaProducer = createKafkaProducer

      override def preStart(): Unit = {
        log.debug( s"Stage SinkStage starting" )
        pull( in )
      }

      setHandler( in, new InHandler {
        def onPush(): Unit = {

          val event = grab( in )
          val messageValue = Json.toJson( event ).toString.getBytes
          val record = new ProducerRecord( topic, event.id.get, messageValue )

          kafkaProducer.send( record )

          pull( in )
        }

        override def onUpstreamFinish(): Unit = {
          log.debug( s"Stage SinkStage finished" )
          promise.success( Done )
          super.onUpstreamFinish()
        }

        override def onUpstreamFailure( ex: Throwable ): Unit = {
          promise.failure( ex )
          super.onUpstreamFailure( ex )
        }
      } )

      override def postStop(): Unit = {
        kafkaProducer.close()
      }
    }

    ( logic, promise.future )
  }

  protected def createKafkaProducer: KafkaProducer[Long, Array[Byte]] = {
    Thread.currentThread().setContextClassLoader( null ) // https://stackoverflow.com/questions/37363119/kafka-producer-org-apache-kafka-common-serialization-stringserializer-could-no

    val props = new Properties
    props.put( "bootstrap.servers", config.getString( "kafka.bootstrap.servers" ).get )
    props.put( "client.id", config.getString( "kafka.client.id" ).get )
    props.put( "key.serializer", config.getString( "kafka.key.serializer" ).get )
    props.put( "value.serializer", config.getString( "kafka.value.serializer" ).get )

    new KafkaProducer[Long, Array[Byte]]( props )
  }

}
