package jp.sysart.fpui.example.multipage

import jp.sysart.fpui.{FunctionalUI => FUI}

import scala.scalajs.LinkingInfo
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import org.scalajs.dom
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.ext.Ajax

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.hot
import slinky.web.html._

import trail._

@JSImport("resources/index.css", JSImport.Default)
@js.native
object IndexCSS extends js.Object

object Main {

  //
  // ROUTE
  //

  object Route {
    val book = Root / "book" / Arg[Int]
  }

  def applyUrlChange(url: URL): (Model, FUI.Effect[Msg]) =
    url.pathname match {
      case Route.book(id) =>
        applySubUpdate(
          BookPage.init(id),
          (m: BookPage.Model) => Model(BookPg(m)),
          BookPageMsg
        )

      case _ =>
        applySubUpdate(
          SearchPage.init(),
          (m: SearchPage.Model) => Model(SearchPg(m)),
          SearchPageMsg
        )
    }

  //
  // MODEL
  //

  case class Model(page: Page)

  sealed trait Page
  case class SearchPg(model: SearchPage.Model) extends Page
  case class BookPg(model: BookPage.Model) extends Page

  def init(url: URL): (Model, FUI.Effect[Msg]) = applyUrlChange(url)

  //
  // UPDATE
  //

  sealed trait Msg
  case class SearchPageMsg(submsg: SearchPage.Msg) extends Msg
  case class BookPageMsg(submsg: BookPage.Msg) extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    (msg, model.page) match {
      case (SearchPageMsg(pageMsg), SearchPg(pageModel)) =>
        applySubUpdate(
          SearchPage.update(pageMsg, pageModel),
          (m: SearchPage.Model) => model.copy(page = SearchPg(m)),
          SearchPageMsg
        )

      case (BookPageMsg(pageMsg), BookPg(pageModel)) =>
        applySubUpdate(
          BookPage.update(pageMsg, pageModel),
          (m: BookPage.Model) => model.copy(page = BookPg(m)),
          BookPageMsg
        )

      case _ => (model, FUI.noEffect)
    }
  }

  def applySubUpdate[SubModel, SubMsg](
      subUpdate: (SubModel, FUI.Effect[SubMsg]),
      applyModel: SubModel => Model,
      applyMsg: SubMsg => Msg
  ): (Model, FUI.Effect[Msg]) = {
    val (subModel, subEffect) = subUpdate
    (
      applyModel(subModel),
      dispatch => subEffect(subMsg => dispatch(applyMsg(subMsg)))
    )
  }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "app")(
      h1(className := "app-title")("Multipage example")
    )
  }

  def main(args: Array[String]): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    new FUI.Runtime(
      dom.document.getElementById("root"),
      FUI.Program(init, view, update)
    )
  }
}
