package jp.sysart.fpui.example.multipage

import io.circe.Decoder

object Domain {

  case class Book(
      workId: Int,
      title: String,
      author: String
  )

  val decoderBook: Decoder[Book] =
    Decoder.forProduct3("workid", "titleweb", "authorweb")(Book)
}
