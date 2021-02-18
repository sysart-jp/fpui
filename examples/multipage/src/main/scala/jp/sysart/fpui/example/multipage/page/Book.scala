package jp.sysart.fpui.example.multipage.page

import cats.effect.IO
import jp.sysart.fpui.{Main, FunctionalUI => FUI}
import jp.sysart.fpui.example.multipage.Domain
import jp.sysart.fpui.example.multipage.Server

import scala.scalajs.js
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

  def init(workId: Int): (Model, IO[Msg]) =
    (
      Model(workId, true, None, None), {
      Server.fetchBook(
        workId,
        result => BookFetched(result),
      ).unsafeRunSync()
      IO(Unit)
    }
    )

  //
  // UPDATE
  //

  sealed trait Msg
  case object Unit extends Msg
  case class BookFetched(result: Either[Throwable, Domain.Book]) extends Msg

  def update(msg: Msg, model: Model): (Model, IO[Msg]) =
    msg match {
      case BookFetched(Right(book)) =>
        (model.copy(loading = false, book = Some(book)), IO(Unit))

      case BookFetched(Left(error)) =>
        (
          model.copy(loading = false, loadingError = Some(error)),
          IO(Unit)
        )

      case _ => (model, IO(Unit))
    }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "book")(
      model.book
        .map(viewBook(_))
        .getOrElse(div(className := "loading")("Loading..."))
    )
  }

  def viewBook(book: Domain.Book): ReactElement = {
    div(
      h1(className := "title")(book.title),
      div(className := "author")(book.author),
      book.about.map(about =>
        div(
          className := "about",
          dangerouslySetInnerHTML := js.Dynamic.literal(__html = about)
        )
      ),
      book.copy.map(copy =>
        div(
          className := "copy",
          dangerouslySetInnerHTML := js.Dynamic.literal(__html = copy)
        )
      )
    )
  }
}
