package com.yuzvin.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import com.yuzvin.http.routes.MessageRoutes

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ApplicationServer extends App with MessageRoutes with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("MLLP-AKKA")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val routes: Route = messageRoutes

  val config = ConfigFactory.load()

  val applicationInterface = config.getString("application.interface")
  val applicationPort = config.getInt("application.port")

  val mllpReceiverHost = config.getString("mllp.receiver.host")
  val mllpReceiverPort = config.getInt("mllp.receiver.port")

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, applicationInterface, applicationPort)

  serverBinding.onComplete {
    case Success(bound) =>
      mllpReceiver.startReceivingMllpMessages(mllpReceiverHost, mllpReceiverPort)
      mllpSender.startInfiniteSendingMessages(mllpReceiverHost, mllpReceiverPort)
      logger.debug(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      logger.error(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)

}
