package jp.sysart.fpui.example.multipage

import jp.sysart.fpui.{FunctionalUI => FUI}
import jp.sysart.fpui.example.multipage.Domain.Book

object BookPage {

  //
  // MODEL
  //

  case class Model(workId: Int, book: Option[Book])

  def init(workId: Int): (Model, FUI.Effect[Msg]) =
    (Model(workId, None), FUI.noEffect)

  //
  // UPDATE
  //

  sealed trait Msg
  case class BookFetched(result: Either[Throwable, Book]) extends Msg
}
