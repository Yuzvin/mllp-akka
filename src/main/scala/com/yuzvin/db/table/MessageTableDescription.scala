package com.yuzvin.db.table
import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._

trait MessageTableDescription {

  case class MessageRow(id: Long, creationTimestamp: Timestamp, content: String, systemName: String)

  case class MessagesTable(tag: Tag) extends Table[MessageRow](tag, "messages") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def created = column[Timestamp]("created")
    def content = column[String]("content")
    def system = column[String]("system")

    override def * = (id, created, content, system) <> (MessageRow.tupled, MessageRow.unapply)
  }

  lazy val Messages = TableQuery[MessagesTable]
}

