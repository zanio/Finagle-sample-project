package com.book

import cats.effect.IO
import com.book.models.RestModels._
import com.book.services.NyTimesService
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, FuturePool}
import io.finch._

import java.time.LocalDate
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  12:04
 */
class RestApi(nyTimesService: NyTimesService) {

  private def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("Healthy")
  }

  // fetch books with the following url params /me/books/list? author="authorName"&year="TheYearThatBookWasPublished"
  private def fetchBooks: Endpoint[IO, Seq[Book]] = get("me" :: "books" :: "list" :: paramOption("author") :: paramOption("year")) {
    (author: Option[String], year: Option[String]) =>
      // Convert year to an Option[Int]
      val yearOption: Option[Int] = year.flatMap(s => scala.util.Try(s.toInt).toOption)
      val fullRequestQuery = s"author=$author&year=$year"
      val requestFromParam = Request(fullRequestQuery)
      nyTimesService.fetchSetup()
  }


  val endpoints =  fetchBooks :+: healthcheck


}
