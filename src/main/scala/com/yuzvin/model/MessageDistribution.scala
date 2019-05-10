package com.yuzvin.model

import java.time.LocalDateTime

object MessageDistributionDateUnits extends Enumeration {
  type MessageDistributionDateUnit = Value
  val Week, Year = Value
}

case class MessageDistribution(startDate: LocalDateTime, endDate: LocalDateTime, messages: List[Message])
