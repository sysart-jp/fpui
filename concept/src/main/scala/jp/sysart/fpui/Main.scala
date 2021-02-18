package jp.sysart.fpui

import cats.effect.IO
import org.scalajs.dom
import org.scalajs.dom.experimental.URL
import slinky.core.facade.ReactElement
import slinky.hot
import slinky.web.html._

import scala.scalajs.LinkingInfo

object Main {

  //
  // MODEL
  //

  case class Model(messages: Seq[String], input: String)

  def init(url: URL): (Model, IO[_]) =
    (Model(Seq.empty, ""), IO())

  //
  // UPDATE
  //

  sealed trait Msg
  case class Input(input: String) extends Msg
  case object Send extends Msg

  def update[Msg](msg: Msg, model: Model): (Model, IO[Msg]) = {
    msg match {
      case Input(input) =>
        (model.copy(input = input), IO(msg))

      case Send =>
        (
          model.copy(messages = model.messages :+ model.input, input = ""),
          IO(msg)
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

    new FunctionalUI.Runtime(
      dom.document.getElementById("root"),
      FunctionalUI.Program(init, view, update[Any])
    )
  }
}
