package jp.sysart.fpui.example.multipage

import scala.util.Success
import scala.util.Failure

import io.circe._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.scalajs.js.URIUtils
import org.scalajs.dom.ext.Ajax

import jp.sysart.fpui.FunctionalUI.Browser
import jp.sysart.fpui.example.multipage.Domain.Book

object Server {

  val bookDecoder: Decoder[Book] =
    Decoder.forProduct3("workid", "titleweb", "authorweb")(Book)

  def searchBooks[Msg](
      query: String,
      createMsg: Either[Throwable, Seq[Book]] => Msg,
      dispatch: Msg => Unit,
      browser: Browser
  ): Unit = {
    val encodedQuery = URIUtils.encodeURIComponent(query)
    browser.ajaxGet(
      "https://reststop.randomhouse.com/resources/works?search=" + encodedQuery,
      Decoder.decodeSeq(bookDecoder),
      createMsg,
      dispatch
    )
  }

  def fetchBook[Msg](
      workId: Int,
      createMsg: Either[Throwable, Book] => Msg,
      dispatch: Msg => Unit,
      browser: Browser
  ): Unit = {
    browser.ajaxGet(
      "https://reststop.randomhouse.com/resources/works/" + workId + "/",
      bookDecoder,
      createMsg,
      dispatch
    )
  }
}
