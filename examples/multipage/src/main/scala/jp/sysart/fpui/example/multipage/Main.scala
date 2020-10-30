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

  //
  // MODEL
  //

  case class Model(page: Page)

  sealed trait Page
  case class SearchPg() extends Page
  case class BookPg(model: BookPage.Model) extends Page

  def init(url: URL): (Model, FUI.Effect[Msg]) =
    url.pathname match {
      case Route.book(id) => {
        val (submodel, subeffect) = BookPage.init(id)
        (
          Model(BookPg(submodel)),
          dispatch => {
            subeffect(submsg => dispatch(BookPageMsg(submsg)))
          }
        )
      }
    }

  //
  // UPDATE
  //

  sealed trait Msg
  case class BookPageMsg(submsg: BookPage.Msg) extends Msg

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    msg match {
      case BookPageMsg(submsg) =>
        (model, FUI.noEffect)
    }
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
