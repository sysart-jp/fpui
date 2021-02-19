package jp.sysart.fpui.example.multipage.page

import cats.effect.IO
import jp.sysart.fpui.FunctionalUI.Browser
import jp.sysart.fpui.example.multipage.Main.Route
import jp.sysart.fpui.example.multipage.{Domain, Server}
import jp.sysart.fpui.{FunctionalUI => FUI}
import slinky.core.facade.ReactElement
import slinky.web.html._

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

  def init(): (Model, IO[Msg]) =
    (Model("", false, None, Seq.empty), IO(Unit))

  def init(query: String): (Model, IO[Msg]) = {
    update(SendQuery, Model(query, false, None, Seq.empty))
  }

  //
  // UPDATE
  //

  sealed trait Msg
  case object Unit extends Msg
  case class QueryInput(query: String) extends Msg
  case object SendQuery extends Msg
  case class SearchResult(result: Either[Throwable, Seq[Domain.Book]])
      extends Msg
  case class FoundItemClicked(workId: Int) extends Msg

  def update(msg: Msg, model: Model): (Model, IO[Msg]) = {
    println(s"Msg:${msg.toString} Model:${model.toString}")
    msg match {
      case QueryInput(query) =>
        (model.copy(query = query), IO(Unit))

      case SendQuery =>
        FUI.Browser.replaceUrl(Route.searchWithQuery.url(model.query))
        (
          model.copy(loading = true),
          Server.searchBooks(model.query, result => SearchResult(result)),
        )

      case SearchResult(Right(books)) =>
        FUI.Browser.replaceUrl(Route.searchWithQuery.url(model.query))
        (
          model.copy(loading = false, loadingError = None, books = books),
          IO(Unit)
        )

      case SearchResult(Left(error)) =>
        (
          model.copy(loading = false, loadingError = Some(error)),
          IO(Unit)
        )

      case FoundItemClicked(workId) =>
        Browser.pushUrl(Route.book.url(workId))
        (model, IO(Unit))

      case _ => (model, IO(Unit))
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
