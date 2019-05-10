package com.yuzvin.service

import java.time.temporal.TemporalAdjusters
import java.time.{ DayOfWeek, LocalDateTime }

import com.yuzvin.db.repository.MessageRepository
import com.yuzvin.model.MessageDistributionDateUnits.MessageDistributionDateUnit
import com.yuzvin.model.{ Message, MessageDistribution, MessageDistributionDateUnits }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MessageService(messageRepository: MessageRepository) {

  def saveMessage(message: Message): Future[Message] = messageRepository.addMessage(message)

  def getLatestMessages(amount: Int, endDate: LocalDateTime): Future[List[Message]] = {
    messageRepository.getLatestMessages(endDate, amount)
  }

  def getDistributedMessages(
    distributedBy: MessageDistributionDateUnit,
    distributionEndDate: Option[LocalDateTime],
    systemName: String): Future[List[MessageDistribution]] = {
    distributedBy match {
      case MessageDistributionDateUnits.Week =>
        val endDate = distributionEndDate.get
        val startDate = endDate.minusWeeks(4)
        messageRepository
          .getMessages(Some(startDate), endDate, systemName)
          .map(messages => distributeMessages(distributedBy, messages))
      case MessageDistributionDateUnits.Year =>
        val endDate = LocalDateTime.now()
        messageRepository
          .getMessages(startDate = None, endDate, systemName)
          .map(messages => distributeMessages(distributedBy, messages))
    }
  }

  def findMessage(id: Int): Future[Option[Message]] = messageRepository.findMessage(id)

  protected def distributeMessages(
    distributedBy: MessageDistributionDateUnit,
    messages: List[Message]): List[MessageDistribution] = {
    distributedBy match {
      case MessageDistributionDateUnits.Week =>
        messages
          .groupBy(m => getStartDayOfWeek(m.creationDate))
          .map {
            case (startDayOfWeek, messagesForWeek) =>
              MessageDistribution(startDayOfWeek, startDayOfWeek.plusWeeks(1), messagesForWeek)
          }
          .toList
      case MessageDistributionDateUnits.Year =>
        messages
          .groupBy(m => getFirstDayOfYear(m.creationDate))
          .map {
            case (firstDayOfYear, messagesForYear) =>
              MessageDistribution(firstDayOfYear, getFirstDayOfNextYear(firstDayOfYear), messagesForYear)
          }
          .toList
    }
  }

  private def getStartDayOfWeek(date: LocalDateTime): LocalDateTime = {
    date.`with`(DayOfWeek.MONDAY).toLocalDate.atStartOfDay()
  }

  private def getFirstDayOfYear(date: LocalDateTime): LocalDateTime = {
    date.`with`(TemporalAdjusters.firstDayOfYear()).toLocalDate.atStartOfDay()
  }

  private def getFirstDayOfNextYear(date: LocalDateTime): LocalDateTime = {
    getFirstDayOfYear(date).plusYears(1).toLocalDate.atStartOfDay()
  }

}
