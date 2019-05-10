package com.yuzvin.model

import java.time.LocalDateTime

case class Message(id: Long, creationDate: LocalDateTime, content: String, systemName: String)
