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

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    (model, FUI.noEffect)
  }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "search")(
      "Search"
    )
  }
}