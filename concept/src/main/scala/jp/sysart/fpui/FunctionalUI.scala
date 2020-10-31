package jp.sysart.fpui

import org.scalajs.dom
import org.scalajs.dom.Element
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.raw.CustomEvent
import org.scalajs.dom.raw.Event

object FunctionalUI {
  type Effect[Msg] = (Browser, (Msg => Unit)) => Unit

  case class Program[Model, Msg](
      init: (URL) => (Model, Effect[Msg]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, Effect[Msg]),
      onUrlChange: Option[URL => Msg] = None
  )

  class Runtime[Model, Msg](container: Element, program: Program[Model, Msg]) {
    private val init = program.init(new URL(dom.window.location.href))
    private var state = init._1

    object browser extends Browser {
      def pushUrl(url: String) = {
        dom.window.history.pushState((), "", url)
        program.onUrlChange.map(_(new URL(dom.window.location.href)))
      }
    }

    def dispatch(msg: Msg): Unit = {
      apply(program.update(msg, state))
    }

    def apply(change: (Model, Effect[Msg])): Unit = {
      val (model, effect) = change
      state = model
      ReactDOM.render(program.view(model, dispatch), container)
      effect(browser, dispatch)
    }

    program.onUrlChange.map(onUrlChange => {
      dom.window.addEventListener(
        "popstate",
        (e: Event) => dispatch(onUrlChange(new URL(dom.window.location.href)))
      )
    })

    apply(init)
  }

  def noEffect[Msg]() = (browser: Browser, dispatch: Msg => Unit) => ()

  trait Browser {
    def pushUrl(url: String)
  }
}
