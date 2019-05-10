package com.yuzvin.mllp

import java.time.LocalDateTime

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.{ IncomingConnection, ServerBinding }
import akka.stream.scaladsl.{ Flow, Framing, Source, Tcp }
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import com.yuzvin.mllp.MllpProtocolSpecification._
import com.yuzvin.model.Message
import com.yuzvin.service.MessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class MllpReceiver(messageService: MessageService) extends LazyLogging {

  val receiveByteString: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(CarriageReturnByte, maximumFrameLength = 20000, allowTruncation = true)

  val byteStringToString: Flow[ByteString, String, NotUsed] = Flow[ByteString]
    .via(receiveByteString)
    .map(trimMessageFromBlockBytes)

  def startReceivingMllpMessages(host: String, port: Int)(implicit system: ActorSystem, materializer: Materializer) = {
    val connections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(host, port)
    connections runForeach { connection =>
      logger.debug(s"New connection to ${connection.localAddress} from: ${connection.remoteAddress}")

      val processConnection =
        byteStringToString.map(storeMessageToDB).mapAsync(parallelism = 10)(createAcknowledgementMessage)

      connection.handleWith(processConnection)
    }
  }

  def trimMessageFromBlockBytes(messageByteString: ByteString): String = {
    if (messageByteString.startsWith(StartBlockByte) && messageByteString.endsWith(EndBlockByte)) {
      val message = messageByteString.utf8String.trim
      logger.debug(s"Received message: $message")
      message
    } else throw new RuntimeException(s"Broken ByteString: $messageByteString")
  }

  def storeMessageToDB(inputMessage: String): Future[Message] = {
    val Array(content, systemName) = inputMessage.split('|')
    val message = Message(id = 0, creationDate = LocalDateTime.now(), content = content, systemName = systemName)

    messageService.saveMessage(message)
  }

  def createAcknowledgementMessage(storingResult: Future[Message]): Future[ByteString] = storingResult.transform {
    case Success(storedMessage) =>
      logger.debug(s"Message successfully stored to database: $storedMessage")
      Success(StartBlockByte ++ SuccessAcknowledgmentMessage ++ EndBlockByte ++ CarriageReturnByte)
    case Failure(exception) =>
      logger.debug("Failed storing message to database")
      exception.printStackTrace()
      Success(StartBlockByte ++ FailAcknowledgmentMessage ++ EndBlockByte ++ CarriageReturnByte)
  }

}
