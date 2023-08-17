package com.book

import cats.effect.IO
import com.book.models.RestModel.ResponseEntity
import com.book.models.WebClientModels.{WcBook, WcBookResponse}
import com.book.services.NyTimesService
import com.book.util.Helper.isValidYear
import com.book.util.Logger
import com.twitter.util.Future
import io.circe._
import io.finch._
import io.finch.catsEffect._
import shapeless.{:+:, CNil}
import com.twitter.finagle.http.Status

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  12:04
 */
class RestApi(nyTimesService: NyTimesService) extends Logger{

  def encodeErrorList(es: List[Exception]): Json = {
    val messages = es.map(x => Json.fromString(x.getMessage))
    Json.obj("errors" -> Json.arr(messages: _*))
  }

  implicit val encodeException: Encoder[Exception] = Encoder.instance({
    case e: io.finch.Errors => encodeErrorList(e.errors.toList)
    case e: io.finch.Error =>
      e.getCause match {
        case e: io.circe.Errors => encodeErrorList(e.errors.toList)
        case err => Json.obj("message" -> Json.fromString(e.getMessage))
      }
    case e: CommonError => Json.obj("message" -> Json.fromString(e.getMessage))
  })


  private val basePath = "me" :: "books" :: "list"
  private val beBlankAuthor = ValidationRule[String](s"be blank") { item =>item.trim.isBlank}
  private val beValidYear = ValidationRule[String](s"be a valid year, e.g '2017' ") { item => isValidYear(item) }

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
      logger.error(s"validation Error : ${e.getMessage}")
      IO(Output.payload(ResponseEntity.failure400(e.getMessage), Status.BadRequest))
    case e =>
      logger.error(s"Internal Server Error : ${e.getMessage}")
      IO(Output.payload(ResponseEntity.failure500(e.getMessage), Status.InternalServerError))
  }

  val endpoints: Endpoint[IO, ResponseEntity :+: String :+: CNil] =  fetchBooks :+: healthcheck


}
