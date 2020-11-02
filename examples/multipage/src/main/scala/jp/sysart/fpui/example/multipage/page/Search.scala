package jp.sysart.fpui.example.multipage.page

import jp.sysart.fpui.{FunctionalUI => FUI}

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

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

  def init(): (Model, FUI.Effect[Msg]) =
    (Model("", false, None, Seq.empty), FUI.noEffect)

  def init(query: String): (Model, FUI.Effect[Msg]) =
    (
      Model(query, false, None, Seq.empty),
      (dispatch, browser) => dispatch(SendQuery)
    )

  //
  // UPDATE
  //

  sealed trait Msg
  case class QueryInput(query: String) extends Msg
  case object SendQuery extends Msg
  case class SearchResult(result: Either[Throwable, Seq[Domain.Book]])
      extends Msg
  case class FoundItemClicked(workId: Int) extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    msg match {
      case QueryInput(query) =>
        (model.copy(query = query), FUI.noEffect)

      case SendQuery =>
        (
          model.copy(loading = true),
          (dispatch, browser) => {
            Server.searchBooks(
              model.query,
              result => SearchResult(result),
              dispatch,
              browser
            )
            browser.replaceUrl(Route.searchWithQuery.url(model.query))
          }
        )

      case SearchResult(Right(books)) =>
        (
          model.copy(loading = false, loadingError = None, books = books),
          FUI.noEffect
        )

      case SearchResult(Left(error)) =>
        (
          model.copy(loading = false, loadingError = Some(error)),
          FUI.noEffect
        )

      case FoundItemClicked(workId) =>
        (model, (dispatch, browser) => browser.pushUrl(Route.book.url(workId)))
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
