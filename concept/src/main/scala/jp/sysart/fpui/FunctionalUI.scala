package jp.sysart.fpui

import scala.util.Success
import scala.util.Failure

import cats.effect.IO

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
  case class Program[Model, Msg](
      init: (URL) => (Model, IO[Option[Msg]]),
      view: (Model, Msg => Unit) => ReactElement,
      update: (Msg, Model) => (Model, IO[Option[Msg]]),
      onUrlChange: Option[URL => Msg] = None
  )

  class Runtime[Model, Msg](
      container: Element,
      val program: Program[Model, Msg]
  ) {
    private val init = program.init(new URL(dom.window.location.href))
    private var state = init._1

    def dispatch(msg: Msg): Unit = {
      apply(program.update(msg, state))
    }

    def apply(change: (Model, IO[Option[Msg]])): Unit = {
      val (model, io) = change
      state = model
      ReactDOM.render(program.view(model, dispatch), container)
      io.unsafeRunAsync {
        case Right(optionMsg) => optionMsg.map(dispatch(_))
        case Left(e)          => throw e // IO without proper error handling
      }
    }

    def onPushUrl(url: URL): Unit =
      program.onUrlChange.map(_(url)).map(dispatch(_))

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

    private var listenersOnPushUrl = Seq[URL => Unit]()

    def runProgram[Model, Msg](
        container: Element,
        program: Program[Model, Msg]
    ) = {
      val runtime = new Runtime(container, program)
      listenersOnPushUrl = listenersOnPushUrl :+ runtime.onPushUrl
    }

    /**
      * Change the URL, but do not trigger a page load.
      * This will add a new entry to the browser history.
      */
    def pushUrl[Msg](url: String): IO[Option[Msg]] =
      IO {
        dom.window.history.pushState((), "", url)
        listenersOnPushUrl.foreach(_(new URL(dom.window.location.href)))
        None
      }

    /**
      * Change the URL, but do not trigger a page load.
      * This will not add a new entry to the browser history.
      */
    def replaceUrl[Msg](url: String): IO[Option[Msg]] =
      IO {
        dom.window.history.replaceState((), "", url)
        None
      }

    def ajaxGet[Msg, Result](
        url: String,
        decoder: Decoder[Result],
        createMsg: Either[Throwable, Result] => Msg
    ): IO[Option[Msg]] =
      IO.async { cb =>
        implicit val resultDecoder = decoder
        Ajax
          .get(url, null, 0, Map("Accept" -> "application/json"))
          .onComplete {
            // Ajax, Decoder のエラーは Msg として補足したいので、
            // IO.async としてはいずれのケースも Right を返す
            case Success(response) => {
              val decoded = decode[Result](response.responseText)
              cb(Right(Some(createMsg(decoded))))
            }
            case Failure(t) => {
              cb(Right(Some(createMsg(Left(t)))))
            }
          }
      }
  }
}
