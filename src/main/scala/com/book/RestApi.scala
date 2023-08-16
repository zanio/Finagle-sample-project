package com.book

import cats.effect.IO
import com.book.models.RestModels._
import com.book.services.NyTimesService
import com.twitter.finagle.http.Request
import cats.effect.IO
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import io.circe._
import io.circe.generic.auto._
import io.finch.Error.NotPresent
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  12:04
 */
class RestApi(nyTimesService: NyTimesService) {

  val basePath = "me" :: "books" :: "list"

  private def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("Healthy")
  }
  // fetch books with the following url params /me/books/list? author="authorName"&year="TheYearThatBookWasPublished"
  private def fetchBooks: Endpoint[IO, Seq[Book]]  = get(basePath :: param("author") :: paramOption("year"))  {
    (author: String, year:Option[String]) =>
      // Convert year to an Option[Int]
      val yearOption: Option[Int] = year.flatMap(s => scala.util.Try(s.toInt).toOption)
      val fullRequestQuery = s"author=$author&year=$year"
      val requestFromParam = Request(fullRequestQuery)
      nyTimesService.fetchSetup().map(books => Ok(books))

  }

  val endpoints =  fetchBooks :+: healthcheck


}
