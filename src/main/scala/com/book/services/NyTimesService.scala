package com.book.services

import com.book.{CommonError, ExternalServiceError}
import com.book.models.WebClientModels._
import com.book.util.Logger
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import com.book.util.Helper._
import com.typesafe.config.Config
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps

import java.time.LocalDate
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  15:45
 */
class NyTimesService(webClient: Service[Request, Response])(implicit val config: Config) extends Logger {
  private val nyTimesToken = config.getString("nytimes.apiKey")
  private val path = config.getString("nytimes.path")

  /**
   * This method is used to get the books from nytimes api, it calls the @{path} with the author and api key
   * @param author
   * @return
   */
  def getBooks(author: String): Future[Either[CommonError,WcBookResponse]] = {
    val bookHistoryPath = path + s"?author=$author&api-key=$nyTimesToken"
    val request = Request(s"${bookHistoryPath}")
    logger.info(s"Request received for getBooks : ${request.path} and the path is : ${bookHistoryPath}")
    request.method(com.twitter.finagle.http.Method.Get)
    val response = webClient(request)
    response.flatMap(resp => {
      val content = resp.getContentString()
      val bookResponse = parseObj[WebclientResponse](content)
      val books = bookResponse.results.map(item => {
        val publishedDate = item.ranks_history.headOption.flatMap(_.published_date.map(_.getYear.toString))
        WcBook(item.title, item.author, publishedDate,item.publisher)
      }).toList
      logger.info(s"Response received for getBooks : ${books.size}")
      Future.value(Right(WcBookResponse(books)))
    })
      .onFailure(ex => {
        logger.error(s"Error occurred while trying to process request : ${ex.getMessage}")
        Future.value(Left(ExternalServiceError(ex.getMessage)))
      })
  }

}
