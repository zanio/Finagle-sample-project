package com.book.services

import com.book.config.AppConfig
import com.book.models.WebClientModels._
import com.book.util.Helper._
import com.book.util.Logger
import com.book.{CommonError, ExternalServiceError}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class NyTimesService(webClient: Service[Request, Response]) extends Logger with AppConfig{

  /**
   * This method is used to get the books from nytimes api,
   * it calls the @{path} with the author and api key
   * @param author
   * @return
   */
  def getBooks(author: String): Future[Either[CommonError,WcBookResponse]] = {
    val bookHistoryPath = path + s"?author=$author&api-key=$nyTimesToken"
    val request = Request(s"${bookHistoryPath}")
    logger.info(s"Request received for getBooks : ${request.path} and the path is : ${bookHistoryPath}")
    request.method(com.twitter.finagle.http.Method.Get)
    val response = webClient(request)
    response.map(resp => {
      val content = resp.getContentString()
      parseObj[WebclientResponse](content) .map { bookResponse =>
        val books = bookResponse.results.map(item => {
          val publishedDate = item.ranks_history.headOption.flatMap(_.published_date.map(_.getYear.toString))
          WcBook(item.title, item.author, publishedDate, item.publisher)
        }).toList
        logger.info(s"Response received for getBooks : ${books.size}")
        WcBookResponse(books)
      }
    })
      .onFailure(ex => {
        logger.error(s"Error occurred while trying to process request : ${ex.getMessage}")
        Future.value(Left(ExternalServiceError(ex.getMessage)))
      })
  }

}
