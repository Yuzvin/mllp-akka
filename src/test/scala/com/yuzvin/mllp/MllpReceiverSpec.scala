package com.yuzvin.mllp
import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.yuzvin.app.modules.AppModule
import com.yuzvin.model.Message
import com.yuzvin.service.MessageService
import org.mockito.ArgumentMatchersSugar._
import org.mockito.IdiomaticMockito
import org.mockito.Mockito.verify
import org.mockito.captor.ArgCaptor
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Future

class MllpReceiverSpec extends WordSpec with Matchers with IdiomaticMockito with ResetMocksAfterEachTest with AppModule {

  override lazy val messageService = mock[MessageService]

  implicit val actorSystem = ActorSystem("MLLP-RECEIVER-TEST")
  implicit val materializer = ActorMaterializer()

  "MLLP receiver" should {
    "process messages" in {
      messageService.saveMessage(*) shouldAnswer ((r: Message) => Future.successful(Message(0, LocalDateTime.now(), r.content, r.systemName)))

      val captor = ArgCaptor[Message]

      val host = "localhost"
      val port = 8083

      mllpReceiver.startReceivingMllpMessages(host, port)
      mllpSender.startInfiniteSendingMessages(host, port)

      Thread.sleep(6000)

      verify(messageService, org.mockito.Mockito.atLeastOnce()).saveMessage(captor.capture)

      val messagesFromFlow = captor.values

      messagesFromFlow.exists(m => m.content == "Patient 1 registered" && m.systemName == "laboratory")
      messagesFromFlow.exists(m => m.content == "Patient 2 registered" && m.systemName == "laboratory")

    }
  }

}
