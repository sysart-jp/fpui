package jp.sysart.fpui

import org.scalajs.dom
import org.scalajs.dom.Element
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import org.scalajs.dom.experimental.URL

object FunctionalUI {
  type Effect[Msg] = (Msg => Unit) => Unit

  case class Program[Model, Msg](
      init: (URL) => (Model, Effect[Msg]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, Effect[Msg])
  )

  class Runtime[Model, Msg](container: Element, program: Program[Model, Msg]) {
    private val init = program.init(new URL(dom.document.location.href))
    private var state = init._1

    def dispatch(msg: Msg): Unit = {
      apply(program.update(msg, state))
    }

    def apply(change: (Model, Effect[Msg])): Unit = {
      val (model, effect) = change
      state = model
      ReactDOM.render(program.view(model, dispatch), container)
      effect(dispatch)
    }

    apply(init)
  }

  def noEffect[Msg]() = (dispatch: Msg => Unit) => ()
}
