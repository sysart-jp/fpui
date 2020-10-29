package jp.sysart.fpui

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSImport}
import scala.scalajs.LinkingInfo
import org.scalajs.dom
import org.scalajs.dom.Element

import slinky.core._
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import slinky.hot
import slinky.web.html._

object FunctionalUI {
  type Effect[Msg] = (Msg => Unit) => Unit

  case class Program[Model, Msg](
      init: () => (Model, Effect[Msg]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, Effect[Msg])
  )

  class Runtime[Model, Msg](container: Element, program: Program[Model, Msg]) {
    private val init = program.init()
    private var state = init._1

    def dispatch(msg: Msg): Unit = {
      apply(program.update(msg, state))
    }

    def apply(change: (Model, Effect[Msg])): Unit = {
      val (model, effect) = change
      state = model
      effect(dispatch)
      ReactDOM.render(program.view(model, dispatch), container)
    }

    apply(init)
  }

  def noEffect[Msg]() = (dispatch: Msg => Unit) => ()
}

object Main {
  case class Model(messages: Seq[String], input: String)

  sealed trait Msg
  case class Input(input: String) extends Msg
  case object Send extends Msg

  def init(): (Model, FunctionalUI.Effect[Msg]) =
    (Model(Seq.empty, ""), FunctionalUI.noEffect)

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

  def update(msg: Msg, model: Model): (Model, FunctionalUI.Effect[Msg]) = {
    msg match {
      case Input(input) =>
        (model.copy(input = input), FunctionalUI.noEffect)
      case Send =>
        (
          model.copy(messages = model.messages :+ model.input, input = ""),
          FunctionalUI.noEffect
        )
    }
  }

  @JSExportTopLevel("main")
  def main(): Unit = {
    if (LinkingInfo.developmentMode) {
      hot.initialize()
    }

    new FunctionalUI.Runtime(
      dom.document.getElementById("root"),
      FunctionalUI.Program(init, view, update)
    )
  }
}
