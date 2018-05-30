package modules

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import javax.inject.{ Inject, Singleton }
import play.api.inject.Module
import play.api.{ Configuration, Environment }

import scala.concurrent.ExecutionContextExecutor

@Singleton
class EventExecutionContext @Inject() ( system: ActorSystem ) extends ExecutionContextExecutor {
  private val dispatcher: MessageDispatcher = system.dispatchers.lookup( "event-processor" )

  override def execute( command: Runnable ) = dispatcher.execute( command )

  override def reportFailure( cause: Throwable ) = dispatcher.reportFailure( cause )
}

class EventContextModule extends Module {
  def bindings( environment: Environment, configuration: Configuration ) =
    Seq( bind[EventExecutionContext].toSelf.eagerly() )
}

