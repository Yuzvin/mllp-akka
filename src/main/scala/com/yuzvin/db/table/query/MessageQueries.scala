package com.yuzvin.db.table.query

import java.sql.Timestamp

import com.yuzvin.db.table.MessageTableDescription
import slick.jdbc.PostgresProfile.api._

trait MessageQueries extends MessageTableDescription {

  def selectMessages(startDate: Option[Timestamp], endDate: Timestamp, systemName: String) = {
    Messages.filter(m => {
      val isCreatedDateBiggerThanStartDate = startDate.map(sd => m.created >= sd).getOrElse(LiteralColumn(true))
      m.system === systemName && m.created < endDate && isCreatedDateBiggerThanStartDate
    }).result
  }

  def selectLatestMessages(endDate: Timestamp, amount: Int) = {
    Messages.filter(_.created <= endDate).sortBy(_.created.desc).take(amount).result
  }

  def selectMessage(id: Long) = Messages.filter(_.id === id).result.headOption

  def insertMessage(messageRow: MessageRow) = {
    (Messages returning Messages.map(_.id)) += messageRow
  }

}
