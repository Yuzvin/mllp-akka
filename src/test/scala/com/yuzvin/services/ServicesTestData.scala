package com.yuzvin.services
import java.time.LocalDateTime

import com.yuzvin.model.{Message, MessageDistribution}

trait ServicesTestData {

  val date    = LocalDateTime.of(2019, 5, 9, 1, 1, 1)
  val message = Message(1, date, "Message 1", "Laboratory")

  val messagesFor2019Year = List(
    message,
    message.copy(creationDate = date.minusDays(1)),
    message.copy(creationDate = date.minusDays(3)),
    message.copy(creationDate = date.minusDays(6)),
    message.copy(creationDate = date.minusDays(9)),
    message.copy(creationDate = date.minusMonths(2)),
  )

  val messagesFor2018Year = messagesFor2019Year.map(m => m.copy(creationDate = m.creationDate.minusYears(1)))

  val messageDistributionByYear = List(
    MessageDistribution(
      startDate = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0),
      endDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0),
      messages = messagesFor2019Year
    ),
    MessageDistribution(
      startDate = LocalDateTime.of(2018, 1, 1, 0, 0, 0, 0),
      endDate = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0),
      messages = messagesFor2018Year
    ),
  )

  private val edgeOf2Weeks: LocalDateTime = LocalDateTime.of(2019, 5, 6, 0, 0, 0, 0)

  val messageDistributionByWeek = List(
    MessageDistribution(
      startDate = edgeOf2Weeks,
      endDate = LocalDateTime.of(2019, 5, 13, 0, 0, 0, 0),
      messages = messagesFor2019Year.filter(m => m.creationDate.isAfter(edgeOf2Weeks))
    ),
    MessageDistribution(
      startDate = LocalDateTime.of(2019, 4, 29, 0, 0, 0, 0),
      endDate = edgeOf2Weeks,
      messages = messagesFor2019Year.filter(m =>
        m.creationDate.isBefore(edgeOf2Weeks) && m.creationDate.getMonthValue >= 4)
    )
  )

}
