package com.book

import cats.effect.IO
import com.book.models.RestModel.ResponseEntity
import com.book.services.NyTimesService
import com.book.util.Helper.isValidYears
import com.book.util.Logger
import com.twitter.finagle.http.Status
import io.finch._
import io.finch.catsEffect._
import shapeless.{:+:, CNil}


final class RestApi(nyTimesService: NyTimesService) extends Logger{


  private val basePath = "me" :: "books" :: "list"
  private val beBlankAuthor = ValidationRule[String](s"be blank") { item =>item.trim.isBlank}
  private val beValidYear: ValidationRule[String] = ValidationRule("be a valid year, e.g. '2017 or 2018,2019'") { isValidYears}


  private val validAuthor: Endpoint[IO, String] = param("author").shouldNot(beBlankAuthor)
    .handle { case e => throw InvalidInput(e.getMessage)}

  private val validYear: Endpoint[IO, Option[String]] = paramOption("year").should(beValidYear)
    . handle( e => throw InvalidInput(e.getMessage))


  private def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("Healthy")
  }

  /**
   * This endpoint will fetch the books from nytimes api
   * @return
   */
  private def fetchBooks: Endpoint[IO, ResponseEntity]  = get(basePath :: validAuthor :: validYear)  {
     (author: String, year: Option[String]) =>
      nyTimesService.getBooks(author).map {
        case Right(value) => Ok(ResponseEntity.success(value.books))
        case Left(error) => throw error
      }

  }.rescue {
    case e: CommonError =>
      logger.error(s"${e.getClass} Error : ${e.getMessage}")
      IO(Output.payload(ResponseEntity.failure400(e.getMessage), Status.BadRequest))
    case e =>
      logger.error(s"Internal Server Error : ${e.getMessage}")
      IO(Output.payload(ResponseEntity.failure500(e.getMessage), Status.InternalServerError))
  }

  val endpoints: Endpoint[IO, ResponseEntity :+: String :+: CNil] =  fetchBooks :+: healthcheck


}
