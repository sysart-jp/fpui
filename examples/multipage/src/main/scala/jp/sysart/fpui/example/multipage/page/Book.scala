package jp.sysart.fpui.example.multipage.page

import jp.sysart.fpui.{FunctionalUI => FUI}
import jp.sysart.fpui.example.multipage.Domain
import jp.sysart.fpui.example.multipage.Server

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

object Book {

  //
  // MODEL
  //

  case class Model(
      workId: Int,
      loading: Boolean,
      loadingError: Option[Throwable],
      book: Option[Domain.Book]
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
  case class BookFetched(result: Either[Throwable, Domain.Book]) extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) =
    msg match {
      case BookFetched(Right(book)) =>
        (model.copy(loading = false, book = Some(book)), FUI.noEffect)

      case BookFetched(Left(error)) =>
        (
          model.copy(loading = false, loadingError = Some(error)),
          FUI.noEffect
        )
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
