package com.yuzvin.http

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.yuzvin.http.responses.MessageDistributionResponse
import com.yuzvin.model.{ Message, MessageDistribution }
import spray.json.{ DefaultJsonProtocol, JsString, JsValue, PrettyPrinter, RootJsonFormat }

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  val datePattern = "dd.MM.yyyy HH:mm:ss"
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(datePattern)

  def parseDate(date: String): LocalDateTime = LocalDateTime.from(dateFormatter.parse(date))

  implicit object LocalDateJsonFormat extends RootJsonFormat[LocalDateTime] {
    override def write(obj: LocalDateTime): JsValue = JsString(obj.format(dateFormatter))

    override def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => parseDate(s)
      case _ => throw new Exception(s"Can't read date from json: $json")
    }
  }

  implicit val printer = PrettyPrinter
  implicit val messageJsonFormat = jsonFormat4(Message)
  implicit val messageDistributionJsonFormat = jsonFormat3(MessageDistribution)
  implicit val messageDistributionResponseFormat = jsonFormat1(MessageDistributionResponse)

}
