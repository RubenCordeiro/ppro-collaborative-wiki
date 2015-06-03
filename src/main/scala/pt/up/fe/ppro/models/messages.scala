package pt.up.fe.ppro.models.messages

sealed abstract class Message extends Product {
  def mType: String = this match { case _: Control => "Control" case _: Chat => "Chat" case _ => "" }
}

sealed trait Control

case class Register(name: String) extends Message with Control

sealed trait Chat

case class Say(content: String) extends Message with Chat
