package com.yuzvin.db

import com.typesafe.scalalogging.LazyLogging
import com.yuzvin.db.table.MessageTableDescription
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

object PostgresDatabaseInitializer extends MessageTableDescription with LazyLogging {

  lazy val db: PostgresProfile.backend.Database = initializeDb()

  def initializeDb() = {
    logger.debug("Initialing db...")
    val database = Database.forConfig("postgres")
    val tables = List(Messages)

    val existing = database.run(MTable.getTables)
    existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables.filter(table => !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
      database.run(DBIO.sequence(createIfNotExist))
    })

    database
  }

}
