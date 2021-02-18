package jp.sysart.fpui.example.multipage

import cats.effect.IO
import io.circe.{Decoder, _}
import jp.sysart.fpui.FunctionalUI.Browser
import jp.sysart.fpui.example.multipage.Domain.Book

import scala.scalajs.js.URIUtils

object Server {

  val bookDecoder: Decoder[Book] =
    Decoder.forProduct5("workid", "titleweb", "authorweb", "rgabout", "rgcopy")(
      Book
    )

  val searchBooksDecoder = new Decoder[Seq[Book]] {
    implicit val bookSeqDecoder = Decoder.decodeSeq(bookDecoder)
    final def apply(c: HCursor): Decoder.Result[Seq[Book]] =
      for {
        books <- c.downField("work").as[Seq[Book]]
      } yield books
  }

  def searchBooks[Msg](
      query: String,
      createMsg: Either[Throwable, Seq[Book]] => Msg,
  ): IO[Msg] = {
    val encodedQuery = URIUtils.encodeURIComponent(query)
    Browser.ajaxGet(
      "https://reststop.randomhouse.com/resources/works?search=" + encodedQuery,
      searchBooksDecoder,
      createMsg
    )
  }

  def fetchBook[Msg](
      workId: Int,
      createMsg: Either[Throwable, Book] => Msg,
  ): IO[Msg] = {
    Browser.ajaxGet(
      "https://reststop.randomhouse.com/resources/works/" + workId + "/",
      bookDecoder,
      createMsg
    )
  }
}
