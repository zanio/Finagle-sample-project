package com.book.models



import shapeless.CNil

import java.time.LocalDate

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  11:41
 */
object WebClientModels {

  import io.circe.{Decoder, Encoder, HCursor, Json}


//  implicit val customConfig: Configuration = Configuration.default.withDefaults
  case class BookHistory(title: String, author: String, price: String, publisher: Option[String], ranks_history: Seq[RankHistory])

  implicit val decodeBookHistory: Decoder[BookHistory] = (c: HCursor) => for {
    title <- c.downField("title").as[String]
    author <- c.downField("author").as[String]
    price <- c.downField("price").as[String]
    publisher <- c.downField("publisher").as[Option[String]]
    ranks_history <- c.downField("ranks_history").as[Seq[RankHistory]]
  } yield {
    BookHistory(title, author, price, publisher, ranks_history)
  }

  // decoder for list of of BookHistory

  case class WebclientResponse(status: String, num_results: Int, results: Seq[BookHistory])

  implicit val decodeWebclientResponse: Decoder[WebclientResponse] = (c: HCursor) => for {
    status <- c.downField("status").as[String]
    num_results <- c.downField("num_results").as[Int]
    results <- c.downField("results").as[Seq[BookHistory]]
  } yield {
    WebclientResponse(status, num_results, results)
  }

  object WebclientResponse {
    def empty = WebclientResponse("", 0, Seq.empty)
  }

  case class WcBook(title: String, author: String, year: Option[String] = None, publisher: Option[String])

  implicit val decodeWcBook: Decoder[WcBook] = (c: HCursor) => for {
    title <- c.downField("title").as[String]
    author <- c.downField("author").as[String]
    year <- c.downField("year").as[Option[String]] // this is not working,
    publisher <- c.downField("publisher").as[Option[String]]
  } yield {
    WcBook(title, author, year, publisher)
  }

  implicit val encodeWcBook: Encoder[WcBook] = (a: WcBook) => Json.obj(
    ("title", Json.fromString(a.title)),
    ("author", Json.fromString(a.author)),
    ("year", Json.fromString(a.year.getOrElse(""))),
    ("publisher", Json.fromString(a.publisher.getOrElse("")))
  )

  case class WcBookResponse(books: List[WcBook])

  implicit val decodeWcBookResponse: Decoder[WcBookResponse] = (c: HCursor) => for {
    results <- c.downField("books").as[List[WcBook]]
  } yield {
    WcBookResponse(results)
  }

  implicit val encodeWcBookResponse: Encoder[WcBookResponse] = (a: WcBookResponse) => Json.obj(
    ("books", Json.fromValues(a.books.map(encodeWcBook.apply)))
  )

  object WcBookResponse {
    def empty = WcBookResponse(List.empty)
  }


  case class RankHistory(rank: Int, weeks_on_list: Int, published_date: Option[LocalDate])

  implicit val decodeRankHistory: Decoder[RankHistory] = (c: HCursor) => for {
    rank <- c.downField("rank").as[Int]
    weeks_on_list <- c.downField("weeks_on_list").as[Int]
    published_date <- c.downField("published_date").as[Option[LocalDate]]
  } yield {
    RankHistory(rank, weeks_on_list, published_date)
  }


}
