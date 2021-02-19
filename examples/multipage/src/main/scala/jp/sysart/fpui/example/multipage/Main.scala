package jp.sysart.fpui.example.multipage

import cats.effect.IO
import jp.sysart.fpui.{FunctionalUI => FUI}

import scala.util.chaining._
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
    val search = Root
    val searchWithQuery = Root & Param[String]("query")
    val book = Root / "book" / Arg[Int]
  }

  //
  // MODEL
  //

  case class Model(currentPage: Page)

  sealed trait Page
  case object NotFoundPage extends Page
  case class SearchPage(pageModel: page.Search.Model) extends Page
  case class BookPage(pageModel: page.Book.Model) extends Page

  def init(url: URL): (Model, IO[Msg]) =
    applyUrlChange(url, Model(NotFoundPage))

  def updatePage(page: Page, model: Model): Model =
    model.copy(currentPage = page)

  //
  // UPDATE
  //

  sealed trait Msg
  case object Unit extends Msg
  case class UrlChanged(url: URL) extends Msg
  case class SearchPageMsg(pageMsg: page.Search.Msg) extends Msg
  case class BookPageMsg(pageMsg: page.Book.Msg) extends Msg

  def update(msg: Msg, model: Model): (Model, IO[Msg]) = {
    println(s"Msg:${msg} Model:$model")

    (msg, model.currentPage) match {
      case (UrlChanged(url), _) =>
        applyUrlChange(url, model)

      case (SearchPageMsg(pageMsg), SearchPage(pageModel)) =>
        applyPageUpdate(
          page.Search.update(pageMsg, pageModel),
          SearchPage,
          SearchPageMsg,
          model
        )

      case (BookPageMsg(pageMsg), BookPage(pageModel)) =>
        applyPageUpdate(
          page.Book.update(pageMsg, pageModel),
          BookPage,
          BookPageMsg,
          model
        )

      case _ =>
        println("undefined message")
        (model, IO(Unit))
    }
  }

  def applyUrlChange(url: URL, model: Model): (Model, IO[Msg]) =
    url.pathname + url.search + url.hash match {
      case Route.searchWithQuery(q) =>
        applyPageUpdate(page.Search.init(q), SearchPage, SearchPageMsg, model)

      case Route.search(_) =>
        applyPageUpdate(page.Search.init(), SearchPage, SearchPageMsg, model)

      case Route.book(id) =>
        applyPageUpdate(page.Book.init(id), BookPage, BookPageMsg, model)

      case _ =>
        (model.copy(currentPage = NotFoundPage), IO(Unit))
    }

  def applySubUpdate[SubModel, SubMsg](
      subUpdate: (SubModel, IO[SubMsg]),
      applyModel: SubModel => Model,
      applyMsg: SubMsg => Msg
  ): (Model, IO[Msg]) = {
    val (subModel, subEffect) = subUpdate
    (
      applyModel(subModel),
      //(dispatch, browser) =>
      //  subEffect(subMsg => dispatch(applyMsg(subMsg)), browser)
      IO(Unit)
    )
  }

  def applyPageUpdate[PageModel, PageMsg](
      subUpdate: (PageModel, IO[PageMsg]),
      wrapModel: PageModel => Page,
      applyMsg: PageMsg => Msg,
      model: Model
  ): (Model, IO[Msg]) =
    applySubUpdate[PageModel, PageMsg](
      subUpdate,
      wrapModel(_).pipe(updatePage(_, model)),
      applyMsg
    )

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
        case NotFoundPage =>
          div(className := "page-not-found")("Page Not Found")

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
      FUI.Program(init, view, update, Some(UrlChanged(_)))
    )
  }
}
