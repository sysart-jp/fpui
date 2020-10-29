package jp.sysart.fpui.example.todo

import jp.sysart.fpui.{FunctionalUI => FUI}

import scala.scalajs.js
import scala.scalajs.LinkingInfo
import org.scalajs.dom

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.hot
import slinky.web.html._

object Main {
  case class Model(messages: Seq[String], input: String)

  sealed trait Msg
  case class Input(input: String) extends Msg
  case object Send extends Msg

  def main(args: Array[String]): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    new FUI.Runtime(
      dom.document.getElementById("root"),
      FUI.Program(init, view, update)
    )
  }

  def init(): (Model, FUI.Effect[Msg]) = (Model(Seq.empty, ""), FUI.noEffect)

  def view(model: Model, dispatch: Msg => Unit): ReactElement = {
    div(className := "app")(
      h1(className := "app-title")("Functional UI example"),
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

  def update(msg: Msg, model: Model): (Model, FUI.Effect[Msg]) = {
    msg match {
      case Input(input) =>
        (model.copy(input = input), FUI.noEffect)
      case Send =>
        (
          model.copy(messages = model.messages :+ model.input, input = ""),
          FUI.noEffect
        )
    }
  }
}
