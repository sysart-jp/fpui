package jp.sysart.fpui

import scala.util.Success
import scala.util.Failure

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.raw.CustomEvent
import org.scalajs.dom.raw.Event
import org.scalajs.dom.ext.Ajax

import slinky.core.facade.ReactElement
import slinky.web.ReactDOM

import io.circe._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object FunctionalUI {
  type Effect[Msg] = ((Msg => Unit), Browser) => Unit

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
      effect(dispatch, browser)
    }

    object browser extends DefaultBrowser {
      override def pushUrl(url: String) = {
        super.pushUrl(url)
        program.onUrlChange
          .map(_(new URL(dom.window.location.href)))
          .map(dispatch(_))
      }
    }

    program.onUrlChange.map(onUrlChange => {
      dom.window.addEventListener(
        "popstate",
        (e: Event) => dispatch(onUrlChange(new URL(dom.window.location.href)))
      )
    })

    apply(init)
  }

  def noEffect[Msg]() = (dispatch: Msg => Unit, browser: Browser) => ()

  trait Browser {

    /**
      * Change the URL, but do not trigger a page load.
      * This will add a new entry to the browser history.
      */
    def pushUrl(url: String): Unit

    /**
      * Change the URL, but do not trigger a page load.
      * This will not add a new entry to the browser history.
      */
    def replaceUrl(url: String): Unit

    def ajaxGet[Msg, Result](
        url: String,
        decoder: Decoder[Result],
        createMsg: Either[Throwable, Result] => Msg,
        dispatch: Msg => Unit
    ): Unit
  }

  class DefaultBrowser extends Browser {
    implicit val ec = scala.concurrent.ExecutionContext.global

    def pushUrl(url: String) = {
      dom.window.history.pushState((), "", url)
    }

    def replaceUrl(url: String) = {
      dom.window.history.replaceState((), "", url)
    }

    def ajaxGet[Msg, Result](
        url: String,
        decoder: Decoder[Result],
        createMsg: Either[Throwable, Result] => Msg,
        dispatch: Msg => Unit
    ): Unit = {
      implicit val resultDecoder = decoder
      Ajax
        .get(url, null, 0, Map("Accept" -> "application/json"))
        .onComplete {
          case Success(response) => {
            val decoded = decode[Result](response.responseText)
            dispatch(createMsg(decoded))
          }
          case Failure(t) => {
            dispatch(createMsg(Left(t)))
          }
        }
    }
  }
}
