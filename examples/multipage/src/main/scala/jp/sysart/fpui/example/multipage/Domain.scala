package jp.sysart.fpui.example.multipage

object Domain {

  case class Book(
      workId: Int,
      title: String,
      author: String,
      about: Option[String],
      copy: Option[String]
  )
}
