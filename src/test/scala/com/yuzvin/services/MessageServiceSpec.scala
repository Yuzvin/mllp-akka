package com.yuzvin.services
import java.time.LocalDateTime

import com.yuzvin.app.modules.AppModule
import com.yuzvin.db.repository.SlickMessageRepository
import com.yuzvin.model.MessageDistributionDateUnits
import org.mockito.ArgumentMatchersSugar._
import org.mockito.IdiomaticMockito
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class MessageServiceSpec
  extends WordSpec
  with Matchers
  with IdiomaticMockito
  with ResetMocksAfterEachTest
  with AppModule
  with ServicesTestData {

  override lazy val messageRepository = mock[SlickMessageRepository]

  "MessageService" should {
    "save message" in {
      messageRepository.addMessage(message) shouldReturn Future.successful(message)
      await(messageService.saveMessage(message)) shouldEqual message
      messageRepository.addMessage(message) wasCalled once
    }

    "get latest messages" in {
      messageRepository.getLatestMessages(any[LocalDateTime], 10) shouldReturn Future.successful(messagesFor2019Year)
      await(messageService.getLatestMessages(10, date)) shouldEqual messagesFor2019Year
      messageRepository.getLatestMessages(any[LocalDateTime], 10) wasCalled once
    }

    "get distributed messages by year by system" in {

      messageRepository.getMessages(startDate = None, endDate = any[LocalDateTime], systemName = "bar") shouldReturn Future
        .successful(messagesFor2019Year ++ messagesFor2018Year)
      await(messageService.getDistributedMessages(MessageDistributionDateUnits.Year, None, "bar")) shouldEqual messageDistributionByYear
      messageRepository.getMessages(startDate = None, endDate = any[LocalDateTime], systemName = "bar") wasCalled once
    }

    "get distributed messages by week by system for 4 weeks from date" in {
      messageRepository.getMessages(startDate = Some(date.minusWeeks(4)), endDate = date, systemName = "bar") shouldReturn Future
        .successful(messagesFor2019Year.take(5))
      await(messageService.getDistributedMessages(MessageDistributionDateUnits.Week, Some(date), "bar")) shouldEqual messageDistributionByWeek
      messageRepository.getMessages(startDate = Some(date.minusWeeks(4)), endDate = date, systemName = "bar") wasCalled once
    }

    "find message by id" in {
      messageRepository.findMessage(1) shouldReturn Future.successful(Some(message))
      await(messageService.findMessage(1)) shouldEqual Some(message)
      messageRepository.findMessage(1) wasCalled once
    }

  }

  def await[T](f: Future[T]): T = Await.result(f, 1.second)
}
