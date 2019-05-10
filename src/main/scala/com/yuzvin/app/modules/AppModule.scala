package com.yuzvin.app.modules
import com.yuzvin.db.PostgresDatabaseInitializer
import com.yuzvin.db.repository.SlickMessageRepository
import com.yuzvin.mllp.{ MllpReceiver, MllpSender }
import com.yuzvin.service.MessageService

trait AppModule {
  import com.softwaremill.macwire._

  lazy val postgresDb = PostgresDatabaseInitializer.db
  lazy val messageRepository: SlickMessageRepository = wire[SlickMessageRepository]
  lazy val messageService: MessageService = wire[MessageService]

  lazy val mllpReceiver = wire[MllpReceiver]
  lazy val mllpSender = wire[MllpSender]

}
