package com.yuzvin.http.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ concat, pathEnd, pathPrefix, _ }
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import com.typesafe.scalalogging.LazyLogging
import com.yuzvin.app.modules.AppModule
import com.yuzvin.http.JsonSupport
import com.yuzvin.model.MessageDistributionDateUnits
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{ Failure, Success, Try }

trait MessageRoutes extends AppModule with JsonSupport with LazyLogging {
  implicit def system: ActorSystem

  implicit def customExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Throwable =>
        extractUri { uri =>
          logger.error(s"Error while processing $uri", e)
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "Internal server error"))
        }
    }

  lazy val messageRoutes: Route =
    pathPrefix("api" / "v1" / "messages") {
      concat(
        pathEnd {
          get {
            parameters('amount.as[Int], 'date.as[String]) { (amountOfLatestMessages, endDateString) =>
              Try(parseDate(endDateString)) match {
                case Success(date) =>
                  complete(messageService.getLatestMessages(amountOfLatestMessages, date))
                case Failure(exception) => complete(StatusCodes.BadRequest -> s"Error during parsing date: ${exception.getMessage}")
              }
            }
          }
        },
        path(Segment) { messageId =>
          get {
            onSuccess(messageService.findMessage(messageId.toInt)) {
              case Some(message) => complete(message)
              case None => complete(StatusCodes.NotFound -> "Message not found")
            }
          }
        },
        path("system" / Segment) { systemName =>
          get {
            parameters('distributedBy.?, 'date.?) {
              case (Some("week"), Some(endDateString)) =>
                Try(parseDate(endDateString)) match {
                  case Success(date) =>
                    complete(messageService.getDistributedMessages(MessageDistributionDateUnits.Week, Some(date), systemName))
                  case Failure(exception) => complete(StatusCodes.BadRequest -> s"Error during parsing date: ${exception.getMessage}")
                }
              case (Some("year"), None) =>
                complete(messageService.getDistributedMessages(MessageDistributionDateUnits.Year, None, systemName))
              case _ => complete(StatusCodes.BadRequest -> "Wrong params, see API description")
            }
          }
        })
    }
}
