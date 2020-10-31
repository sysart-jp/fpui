package jp.sysart.fpui

import org.scalajs.dom
import org.scalajs.dom.Element
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.raw.CustomEvent
import org.scalajs.dom.raw.Event

object FunctionalUI {
  type Effect[Msg] = (Msg => Unit) => Unit

  case class Program[Model, Msg](
      init: (URL) => (Model, Effect[Msg]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, Effect[Msg]),
      onUrlChange: Option[URL => Msg] = None
  )

  class Runtime[Model, Msg](container: Element, program: Program[Model, Msg]) {
    private val init = program.init(new URL(dom.window.location.href))
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

    program.onUrlChange.map(onUrlChange => {
      val listener =
        (e: Event) => dispatch(onUrlChange(new URL(dom.window.location.href)))
      dom.window.addEventListener("popstate", listener)
      dom.window.addEventListener(pushUrlEventType, listener)
    })

    apply(init)
  }

  def noEffect[Msg]() = (dispatch: Msg => Unit) => ()

  private val pushUrlEventType = "pushurl"

  def pushUrl(url: String) = {
    // Calling history.pushState() or history.replaceState() won't trigger any event by default.
    // https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onpopstate
    dom.window.history.pushState((), "", url)
    dom.window.dispatchEvent(new CustomEvent(pushUrlEventType, ()))
  }
}
