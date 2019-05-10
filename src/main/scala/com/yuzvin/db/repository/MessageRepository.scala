package com.yuzvin.db.repository

import java.sql.Timestamp
import java.time.LocalDateTime

import com.yuzvin.db.table.query.MessageQueries
import com.yuzvin.model.Message
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MessageRepository {

  def getMessages(startDate: Option[LocalDateTime], endDate: LocalDateTime, systemName: String): Future[List[Message]]

  def getLatestMessages(endDate: LocalDateTime, amount: Int): Future[List[Message]]

  def findMessage(id: Int): Future[Option[Message]]

  def addMessage(message: Message): Future[Message]

}

class SlickMessageRepository(db: Database) extends MessageRepository with MessageQueries {

  def getMessages(startDate: Option[LocalDateTime], endDate: LocalDateTime, systemName: String): Future[List[Message]] = {
    val startTimestamp = startDate.map(dateToTimestamp)
    val endTimestamp = dateToTimestamp(endDate)

    db.run(selectMessages(startTimestamp, endTimestamp, systemName)).map(_.map(messageRowToMessage).toList)
  }

  def getLatestMessages(endDate: LocalDateTime, amount: Int): Future[List[Message]] = {
    val endTimestamp = dateToTimestamp(endDate)

    db.run(selectLatestMessages(endTimestamp, amount)).map(_.map(messageRowToMessage).toList)
  }

  def findMessage(id: Int): Future[Option[Message]] = {
    db.run(selectMessage(id)).map(_.map(messageRowToMessage))
  }

  def addMessage(message: Message): Future[Message] = {
    val messageRow = messageToMessageRow(message)

    db.run(insertMessage(messageRow)).map(newId => message.copy(id = newId))
  }

  private def messageRowToMessage(row: MessageRow): Message = {
    Message(
      id = row.id,
      creationDate = timestampToDate(row.creationTimestamp),
      content = row.content,
      systemName = row.systemName)
  }

  private def messageToMessageRow(message: Message): MessageRow = {
    MessageRow(
      id = message.id,
      creationTimestamp = dateToTimestamp(message.creationDate),
      content = message.content,
      systemName = message.systemName)
  }

  private def dateToTimestamp(date: LocalDateTime): Timestamp = Timestamp.valueOf(date)

  private def timestampToDate(timestamp: Timestamp): LocalDateTime = timestamp.toLocalDateTime

}
