package jp.sysart.fpui.example.multipage

import scala.util.Success
import scala.util.Failure

import io.circe._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import org.scalajs.dom.ext.Ajax

import jp.sysart.fpui.FunctionalUI.Browser
import jp.sysart.fpui.example.multipage.Domain.Book

object Server {

  val bookDecoder: Decoder[Book] =
    Decoder.forProduct3("workid", "titleweb", "authorweb")(Book)

  def fetchBook[Msg](
      workId: Int,
      createMsg: Either[Throwable, Book] => Msg,
      browser: Browser,
      dispatch: Msg => Unit
  ): Unit = {
    browser.fetchOne[Msg, Book](
      "https://reststop.randomhouse.com/resources/works/" + workId + "/",
      bookDecoder,
      createMsg,
      dispatch
    )
  }
}
