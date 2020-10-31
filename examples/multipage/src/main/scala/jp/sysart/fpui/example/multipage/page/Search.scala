package jp.sysart.fpui.example.multipage.page

import jp.sysart.fpui.{FunctionalUI => FUI}

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.html._

object Search {

  //
  // MODEL
  //

  case class Model(
      query: String,
      loading: Boolean,
      loadingError: Option[Throwable]
  )

  def init(): (Model, FUI.Effect[Msg]) =
    (Model("", false, None), FUI.noEffect)

  //
  // UPDATE
  //

  sealed trait Msg
  case class QueryInput(query: String) extends Msg
  case object SendQuery extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    msg match {
      case QueryInput(query) =>
        (model.copy(query = query), FUI.noEffect)

      case SendQuery =>
        (model, (dispatch, browser) => browser.pushUrl("/book/118711"))
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
          value := model.query,
          onInput := ((e) => dispatch(QueryInput(e.target.value)))
        ),
        button(
          disabled := model.query.trim().isEmpty() || model.loading,
          onClick := ((e) => dispatch(SendQuery))
        )("Search")
      ),
      div(className := "search-results")()
    )
  }
}
