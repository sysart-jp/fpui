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
          page.Book.init(id),
          BookPage.andThen(Model),
          BookPageMsg
        )

      case _ =>
        applySubUpdate(
          page.Search.init(),
          SearchPage.andThen(Model),
          SearchPageMsg
        )
    }

  //
  // MODEL
  //

  case class Model(currentPage: Page)

  sealed trait Page
  case class SearchPage(pageModel: page.Search.Model) extends Page
  case class BookPage(pageModel: page.Book.Model) extends Page

  def init(url: URL): (Model, FUI.Effect[Msg]) = applyUrlChange(url)

  //
  // UPDATE
  //

  sealed trait Msg
  case class UrlChanged(url: URL) extends Msg
  case class SearchPageMsg(pageMsg: page.Search.Msg) extends Msg
  case class BookPageMsg(pageMsg: page.Book.Msg) extends Msg

  val onUrlChange = (url: URL) => UrlChanged(url)

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) =
    (msg, model.currentPage) match {
      case (UrlChanged(url), _) =>
        applyUrlChange(url)

      case (SearchPageMsg(pageMsg), SearchPage(pageModel)) =>
        applySubUpdate(
          page.Search.update(pageMsg, pageModel),
          (m: page.Search.Model) => model.copy(currentPage = SearchPage(m)),
          SearchPageMsg
        )

      case (BookPageMsg(pageMsg), BookPage(pageModel)) =>
        applySubUpdate(
          page.Book.update(pageMsg, pageModel),
          (m: page.Book.Model) => model.copy(currentPage = BookPage(m)),
          BookPageMsg
        )

      case _ => (model, FUI.noEffect)
    }

  def applySubUpdate[SubModel, SubMsg](
      subUpdate: (SubModel, FUI.Effect[SubMsg]),
      applyModel: SubModel => Model,
      applyMsg: SubMsg => Msg
  ): (Model, FUI.Effect[Msg]) = {
    val (subModel, subEffect) = subUpdate
    (
      applyModel(subModel),
      (dispatch, browser) =>
        subEffect(subMsg => dispatch(applyMsg(subMsg)), browser)
    )
  }

  //
  // VIEW
  //

  val css = IndexCSS

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    def mapDispatch[SubMsg](wrap: SubMsg => Msg) =
      (subMsg: SubMsg) => dispatch(wrap(subMsg))

    div(className := "app")(
      div(className := "app-header")(),
      model.currentPage match {
        case SearchPage(pageModel) =>
          page.Search.view(pageModel, mapDispatch(SearchPageMsg(_)))

        case BookPage(pageModel) =>
          page.Book.view(pageModel, mapDispatch(BookPageMsg(_)))
      }
    )
  }

  def main(args: Array[String]): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    new FUI.Runtime(
      dom.document.getElementById("root"),
      FUI.Program(init, view, update, Some(onUrlChange))
    )
  }
}
