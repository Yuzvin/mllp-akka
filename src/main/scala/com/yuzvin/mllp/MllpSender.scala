package com.yuzvin.mllp

import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, GraphDSL, RunnableGraph, Sink, Source, Tcp }
import akka.stream.{ ClosedShape, Materializer, ThrottleMode }
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import com.yuzvin.mllp.MllpProtocolSpecification.{ CarriageReturnByte, EndBlockByte, StartBlockByte }

import scala.concurrent.duration._
import scala.util.Random

class MllpSender extends LazyLogging {

  def startInfiniteSendingMessages(receiverHost: String, receiverPort: Int)(implicit system: ActorSystem, materializer: Materializer) = {
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val infiniteMessages = Source((1 to 100000).map(generateMessage).toList)
      val printReply = Sink.foreach[ByteString](repl => logger.debug("Received acknowledge message: " + repl.utf8String.trim))
      val delay = Flow[String].throttle(1, 2.second, 1, ThrottleMode.shaping)

      val encloseMessage = Flow[String].map { stringToMllpMessage }
      val sendMessageToClient = Tcp().outgoingConnection(receiverHost, receiverPort)

      infiniteMessages ~> delay ~> encloseMessage ~> sendMessageToClient.async ~> printReply.async

      ClosedShape
    }).run()
  }

  private def stringToMllpMessage(msg: String) = {
    StartBlockByte ++ ByteString(msg) ++ EndBlockByte ++ CarriageReturnByte
  }

  private def generateMessage(i: Int): String = s"Patient $i registered|laboratory"

}
