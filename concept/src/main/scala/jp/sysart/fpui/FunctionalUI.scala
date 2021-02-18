package jp.sysart.fpui

import cats.effect.IO
import io.circe.Decoder
import io.circe.parser._
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Event
import slinky.core.facade.ReactElement
import slinky.web.ReactDOM

import scala.util.{Failure, Success}

object FunctionalUI {
  case class Program[Model, Msg](
      init: (URL) => (Model, IO[Msg]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, IO[Msg]),
      onUrlChange: Option[URL => Msg] = None
  )

  class Runtime[Model, Msg](container: Element, program: Program[Model, Msg]) {
    private val init = program.init(new URL(dom.window.location.href))
    private var state = init._1

    def dispatch(msg: Msg): Unit = {
      apply(program.update(msg, state))
    }

    def apply(change: (Model, IO[Msg])): Unit = {
      val (model, effect) = change
      state = model
      ReactDOM.render(program.view(model, dispatch), container)
      effect.unsafeRunSync()
    }

    program.onUrlChange.map(onUrlChange => {
      dom.window.addEventListener(
        "popstate",
        (e: Event) => dispatch(onUrlChange(new URL(dom.window.location.href)))
      )
    })

    apply(init)
  }

  object Browser {
    implicit val ec = scala.concurrent.ExecutionContext.global

    def pushUrl[Msg](url: String): IO[Msg] = IO.async {cb =>
      dom.window.history.pushState((), "", url)
      /*
      program.onUrlChange
        .map(_(new URL(dom.window.location.href)))
        .map(dispatch(_))
       */
    }

    def replaceUrl[Msg](url: String): IO[Msg] = IO.async {cb =>
      dom.window.history.replaceState((), "", url)
    }

    def ajaxGet[Msg, Result](
        url: String,
        decoder: Decoder[Result],
        createMsg: Either[Throwable, Result] => Msg
    ): IO[Msg] = {
      implicit val resultDecoder = decoder
      IO.async { cb =>
        Ajax
          .get(url, null, 0, Map("Accept" -> "application/json"))
          .onComplete {
            case Success(response) => {
              val decoded = decode[Result](response.responseText)
              cb(Right(createMsg(decoded)))
            }
            case Failure(t) => {
              cb(Left(t))
            }
          }
      }
    }
  }
}
