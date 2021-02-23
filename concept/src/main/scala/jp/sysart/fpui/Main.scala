package jp.sysart.fpui

import scala.scalajs.js
import scala.scalajs.LinkingInfo
import org.scalajs.dom

import cats.effect.IO

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.hot
import slinky.web.html._
import org.scalajs.dom.experimental.URL

object Main {

  //
  // MODEL
  //

  case class Model(messages: Seq[String], input: String)

  def init(url: URL): (Model, IO[Option[Msg]]) =
    (Model(Seq.empty, ""), IO.none)

  //
  // UPDATE
  //

  sealed trait Msg
  case class Input(input: String) extends Msg
  case object Send extends Msg

  def update(msg: Msg, model: Model): (Model, IO[Option[Msg]]) = {
    msg match {
      case Input(input) =>
        (model.copy(input = input), IO.none)

      case Send =>
        (
          model.copy(messages = model.messages :+ model.input, input = ""),
          IO.none
        )
    }
  }

  //
  // VIEW
  //

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "app")(
      h1(className := "app-title")("Hello, Functional UI!"),
      div(className := "message-input")(
        input(
          value := model.input,
          onInput := ((e) => dispatch(Input(e.target.value)))
        ),
        button(onClick := ((e) => dispatch(Send)))("Send")
      ),
      div(className := "messages")(
        model.messages.map(message => div(className := "message")(message))
      )
    )
  }

  def main(args: Array[String]): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    FunctionalUI.Browser.runProgram(
      dom.document.getElementById("root"),
      FunctionalUI.Program(init, view, update)
    )
  }
}
