package com.yuzvin.routes
import java.time.LocalDateTime

import com.yuzvin.model.{ Message, MessageDistribution }

trait RoutesTestData {
  val date = LocalDateTime.of(2019, 5, 9, 1, 1, 1)
  val messages = List(
    Message(1, date, "Message 1", "Laboratory"),
    Message(2, date, "Message 2", "Bar"))

  val messageDistributions = List(
    MessageDistribution(
      LocalDateTime.of(2019, 1, 1, 0, 0, 0),
      LocalDateTime.of(2019, 12, 1, 0, 0, 0),
      messages))

}
