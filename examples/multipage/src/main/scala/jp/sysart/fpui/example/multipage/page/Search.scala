package jp.sysart.fpui.example.multipage.page

import jp.sysart.fpui.{FunctionalUI => FUI}

import cats.effect.IO

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

import jp.sysart.fpui.{FunctionalUI => FUI}
import jp.sysart.fpui.example.multipage.Main.Route
import jp.sysart.fpui.example.multipage.Domain
import jp.sysart.fpui.example.multipage.Server

object Search {

  //
  // MODEL
  //

  case class Model(
      query: String,
      loading: Boolean,
      loadingError: Option[Throwable],
      books: Seq[Domain.Book]
  )

  def init(): (Model, IO[Option[Msg]]) =
    (Model("", false, None, Seq.empty), IO.none)

  def init(query: String): (Model, IO[Option[Msg]]) =
    (Model(query, false, None, Seq.empty), IO(Some(SendQuery)))

  //
  // UPDATE
  //

  sealed trait Msg
  case class QueryInput(query: String) extends Msg
  case object SendQuery extends Msg
  case class SearchResult(result: Either[Throwable, Seq[Domain.Book]])
      extends Msg
  case class FoundItemClicked(workId: Int) extends Msg

  def update(msg: Msg, model: Model): (Model, IO[Option[Msg]]) = {
    msg match {
      case QueryInput(query) =>
        (model.copy(query = query), IO.none)

      case SendQuery =>
        (
          model.copy(loading = true),
          FUI.Browser
            .replaceUrl(Route.searchWithQuery.url(model.query))
            .flatMap(_ =>
              Server.searchBooks(model.query, result => SearchResult(result))
            )
        )

      case SearchResult(Right(books)) =>
        (
          model.copy(loading = false, loadingError = None, books = books),
          IO.none
        )

      case SearchResult(Left(error)) =>
        (
          model.copy(loading = false, loadingError = Some(error)),
          IO.none
        )

      case FoundItemClicked(workId) =>
        (model, FUI.Browser.pushUrl(Route.book.url(workId)))
    }
  }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "search")(
      h1("Book Search"),
      div(className := "search-query")(
        input(
          `type` := "text",
          placeholder := "Title or Author Name (e.g. Haruki)",
          value := model.query,
          onInput := ((e) => dispatch(QueryInput(e.target.value)))
        ),
        button(
          disabled := model.query.trim().isEmpty() || model.loading,
          onClick := ((e) => dispatch(SendQuery))
        )("Search")
      ),
      if (model.loading)
        div(className := "loading")("Loading...")
      else
        div(className := "search-results")(
          model.books.map(book =>
            div(className := "found-item", key := book.workId.toString())(
              a(
                className := "title",
                href := "#",
                onClick := ((e) => {
                  e.preventDefault()
                  dispatch(FoundItemClicked(book.workId))
                })
              )(book.title),
              div(className := "author")(book.author)
            )
          )
        )
    )
  }
}
