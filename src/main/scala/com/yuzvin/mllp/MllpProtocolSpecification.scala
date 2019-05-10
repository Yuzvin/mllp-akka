package com.yuzvin.mllp
import akka.util.ByteString

object MllpProtocolSpecification {

  val StartBlockByte = ByteString(0x0b)
  val EndBlockByte = ByteString(0x0c)
  val CarriageReturnByte = ByteString(0x0d)

  val SuccessAcknowledgmentMessage = ByteString("ACK")
  val FailAcknowledgmentMessage = ByteString("NACK")

}
