package com.yuzvin.routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.yuzvin.http.routes.MessageRoutes
import com.yuzvin.model.MessageDistributionDateUnits
import com.yuzvin.service.MessageService
import org.mockito.IdiomaticMockito
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Future

class MessageRoutesSpec
  extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with IdiomaticMockito
  with ResetMocksAfterEachTest
  with RoutesTestData
  with MessageRoutes {

  override lazy val messageService: MessageService = mock[MessageService]

  "MessageRoutes" should {
    "return N messages" in {

      messageService.getLatestMessages(10, date) shouldReturn Future.successful(messages)

      val request = HttpRequest(uri = "/api/v1/messages?amount=10&date=09.05.2019%2001:01:01")

      request ~> messageRoutes ~> check {
        messageService.getLatestMessages(10, date) wasCalled once

        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[String] shouldEqual """[{
                                 |  "content": "Message 1",
                                 |  "creationDate": "09.05.2019 01:01:01",
                                 |  "id": 1,
                                 |  "systemName": "Laboratory"
                                 |}, {
                                 |  "content": "Message 2",
                                 |  "creationDate": "09.05.2019 01:01:01",
                                 |  "id": 2,
                                 |  "systemName": "Bar"
                                 |}]""".stripMargin
      }
    }

    "return message by id" in {
      messageService.findMessage(1) shouldReturn Future.successful(Some(messages.head))
      val request = HttpRequest(uri = "/api/v1/messages/1")
      request ~> messageRoutes ~> check {
        messageService.findMessage(1) wasCalled once

        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[String] shouldEqual """{
                               |  "content": "Message 1",
                               |  "creationDate": "09.05.2019 01:01:01",
                               |  "id": 1,
                               |  "systemName": "Laboratory"
                               |}""".stripMargin
      }
    }

    "return NotFound when message is not found" in {
      messageService.findMessage(1) shouldReturn Future.successful(None)
      val request = HttpRequest(uri = "/api/v1/messages/1")
      request ~> Route.seal(messageRoutes) ~> check {
        messageService.findMessage(1) wasCalled once

        status shouldEqual StatusCodes.NotFound
        entityAs[String] shouldEqual "Message not found"
      }
    }

    "return distribution of messages by years by system" in {
      messageService.getDistributedMessages(MessageDistributionDateUnits.Year, None, "bar") shouldReturn Future.successful(messageDistributions)

      val request = HttpRequest(uri = "/api/v1/messages/system/bar?distributedBy=year")

      request ~> messageRoutes ~> check {
        messageService.getDistributedMessages(MessageDistributionDateUnits.Year, None, "bar") wasCalled once

        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[String] shouldEqual """[{
                                       |  "endDate": "01.12.2019 00:00:00",
                                       |  "messages": [{
                                       |    "content": "Message 1",
                                       |    "creationDate": "09.05.2019 01:01:01",
                                       |    "id": 1,
                                       |    "systemName": "Laboratory"
                                       |  }, {
                                       |    "content": "Message 2",
                                       |    "creationDate": "09.05.2019 01:01:01",
                                       |    "id": 2,
                                       |    "systemName": "Bar"
                                       |  }],
                                       |  "startDate": "01.01.2019 00:00:00"
                                       |}]""".stripMargin
      }
    }

    "return distribution of messages by week by system for last four weeks by date" in {
      messageService.getDistributedMessages(MessageDistributionDateUnits.Week, Some(date), "bar") shouldReturn Future.successful(messageDistributions)

      val request = HttpRequest(uri = "/api/v1/messages/system/bar?distributedBy=week&date=09.05.2019%2001:01:01")

      request ~> messageRoutes ~> check {
        messageService.getDistributedMessages(MessageDistributionDateUnits.Week, Some(date), "bar") wasCalled once

        status shouldEqual StatusCodes.OK
        contentType shouldEqual ContentTypes.`application/json`
        entityAs[String] shouldEqual """[{
                                       |  "endDate": "01.12.2019 00:00:00",
                                       |  "messages": [{
                                       |    "content": "Message 1",
                                       |    "creationDate": "09.05.2019 01:01:01",
                                       |    "id": 1,
                                       |    "systemName": "Laboratory"
                                       |  }, {
                                       |    "content": "Message 2",
                                       |    "creationDate": "09.05.2019 01:01:01",
                                       |    "id": 2,
                                       |    "systemName": "Bar"
                                       |  }],
                                       |  "startDate": "01.01.2019 00:00:00"
                                       |}]""".stripMargin
      }
    }

    "return BadRequest for malformed Int input in /api/v1/messages" in {
      val request = HttpRequest(uri = "/api/v1/messages?amount=f")

      request ~> Route.seal(messageRoutes) ~> check {
        messageService wasNever called
        status shouldEqual StatusCodes.BadRequest
        entityAs[String].startsWith("The query parameter 'amount' was malformed:") shouldBe true
      }
    }

    "return BadRequest for malformed Date input in /api/v1/messages/system/" in {
      val request = HttpRequest(uri = "/api/v1/messages/system/bar?distributedBy=week&date=13.13.2019%2001:01:01")

      request ~> Route.seal(messageRoutes) ~> check {
        messageService wasNever called
        status shouldEqual StatusCodes.BadRequest
        entityAs[String].startsWith("Error during parsing date:") shouldBe true
      }
    }

    "return BadRequest for wrong params combination in /api/v1/messages/system/" in {
      val request = HttpRequest(uri = "/api/v1/messages/system/bar?distributedBy=year&date=09.05.2019%2001:01:01")

      request ~> Route.seal(messageRoutes) ~> check {
        messageService wasNever called
        status shouldEqual StatusCodes.BadRequest
        entityAs[String] shouldEqual "Wrong params, see API description"
      }
    }

    "return InternalServerError in case of exception" in {
      messageService.getLatestMessages(10, date) shouldReturn Future.failed(new RuntimeException("MessageRouteSpec: malformed amount"))
      val request = HttpRequest(uri = "/api/v1/messages?amount=10&date=09.05.2019%2001:01:01")

      request ~> Route.seal(messageRoutes) ~> check {
        messageService.getLatestMessages(10, date) wasCalled once
        status shouldEqual StatusCodes.InternalServerError
        entityAs[String] shouldEqual "Internal server error"
      }
    }
  }
}
