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

package controllers.storageBackends

import java.security.MessageDigest

import akka.stream.scaladsl._
import akka.stream.stage.{ GraphStageLogic, GraphStageWithMaterializedValue, InHandler }
import akka.stream.{ Attributes, Inlet, SinkShape }
import akka.util.ByteString
import org.apache.commons.codec.binary.Hex
import play.api.libs.streams.Accumulator
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.{ Future, Promise }

/**
 * Created by jeberle on 07.07.17.
 */
trait ObjectBackend extends StorageBackend {
  def read( request: RequestHeader, bucket: String, name: String ): Option[Source[ByteString, _]]
  def write( request: RequestHeader, bucket: String, name: String, callback: ( Any, Future[String] ) => Any ): Accumulator[ByteString, Result]
  def duplicateFile( request: RequestHeader, fromBucket: String, fromName: String, toBucket: String, toName: String ): Boolean

  class ChecksumSink extends GraphStageWithMaterializedValue[SinkShape[ByteString], Future[String]] {
    val in: Inlet[ByteString] = Inlet( "ChecksumSink" )
    override val shape: SinkShape[ByteString] = SinkShape( in )

    override def createLogicAndMaterializedValue( inheritedAttributes: Attributes ): ( GraphStageLogic, Future[String] ) = {
      val promise = Promise[String]()

      ( new GraphStageLogic( shape ) {

        var digest: MessageDigest = MessageDigest.getInstance( "SHA-256" )

        // This requests one element at the Sink startup.
        override def preStart(): Unit = pull( in )

        setHandler( in, new InHandler {
          override def onPush(): Unit = {
            val ii = grab( in )
            digest.update( ii.asByteBuffer )
            pull( in )
          }

          override def onUpstreamFailure( ex: Throwable ): Unit = {
            promise.failure( ex )
            super.onUpstreamFailure( ex )
          }

          override def onUpstreamFinish(): Unit = {
            promise.success( new String( Hex.encodeHex( digest.digest(), true ) ) )
            super.onUpstreamFinish()
          }
        } )

      }, promise.future )
    }
  }
}
