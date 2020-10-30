package jp.sysart.fpui.example.multipage

import jp.sysart.fpui.{FunctionalUI => FUI}
import jp.sysart.fpui.example.multipage.Domain.Book

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

object BookPage {

  //
  // MODEL
  //

  case class Model(
      workId: Int,
      loading: Boolean,
      loadingError: Option[Throwable],
      book: Option[Book]
  )

  def init(workId: Int): (Model, FUI.Effect[Msg]) =
    (
      Model(workId, true, None, None),
      dispatch => {
        Server.fetchBook(workId, result => BookFetched(result), dispatch)
      }
    )

  //
  // UPDATE
  //

  sealed trait Msg
  case class BookFetched(result: Either[Throwable, Book]) extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    msg match {
      case BookFetched(result) =>
        result match {
          case Right(book) =>
            (model.copy(loading = false, book = Some(book)), FUI.noEffect)
          case Left(error) =>
            (
              model.copy(loading = false, loadingError = Some(error)),
              FUI.noEffect
            )
        }
    }
  }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "book")(
      model.book.map(book => {
        div(
          h1(className := "title")(book.title),
          div(className := "author")(book.author)
        )
      })
    )
  }
}
