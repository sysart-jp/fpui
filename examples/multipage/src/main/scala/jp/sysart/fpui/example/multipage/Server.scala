package jp.sysart.fpui.example.multipage

import scala.util.Success
import scala.util.Failure

import io.circe._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import org.scalajs.dom.ext.Ajax

import jp.sysart.fpui.example.multipage.Domain.Book

object Server {

  implicit val decoderBook: Decoder[Book] =
    Decoder.forProduct3("workid", "titleweb", "authorweb")(Book)

  implicit val ec = scala.concurrent.ExecutionContext.global

  def fetchBook[Msg](
      workId: Int,
      createMsg: Either[Throwable, Book] => Msg,
      dispatch: Msg => Unit
  ): Unit = {
    ajaxGet("https://reststop.randomhouse.com/resources/works/" + workId + "/")
      .onComplete {
        case Success(response) => {
          val decoded = decode[Book](response.responseText)
          dispatch(createMsg(decoded))
        }
        case Failure(t) => {
          dispatch(createMsg(Left(t)))
        }
      }
  }

  private def ajaxGet(url: String) =
    Ajax.get(url, null, 0, Map("Accept" -> "application/json"))
}
