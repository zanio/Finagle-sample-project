package com.book.services

import com.book.config.AppConfig
import com.book.models.WebClientModels._
import com.book.util.Helper._
import com.book.util.Logger
import com.book.{CommonError, ExternalServiceError}
import com.twitter.concurrent.AsyncMeter
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.util.DefaultTimer.Implicit
import com.twitter.util.{Duration, Future}

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
    response.flatMap(resp => {
      val content = resp.getContentString()
      val firstRequest = parseObj[WebclientResponse](content) .map { bookResponse =>
        val books = bookResponse.results.map(item => {
          val publishedDate = item.ranks_history.headOption.flatMap(_.published_date.map(_.getYear.toString))
          WcBook(item.title, item.author, publishedDate, item.publisher)
        }).toList
        var size = bookResponse.results.size
        val numberOfRequest = Math.round(if(size > 0)bookResponse.num_results/size else 0)
        logger.info(s"Number of request to be made : ${numberOfRequest}")
        logger.info(s"Response received for getBooks : ${books.size}")
        (numberOfRequest, books)
      }
      val firstWcBookResponse = firstRequest.map(_._2)
      val eitherNumberOfRequest = firstRequest.map(_._1)
      val totalBooks = eitherNumberOfRequest match {
        case Left(_) => Future.value(Seq(firstWcBookResponse))
        case Right(value) => if(value > 1)makeNestedRequest(bookHistoryPath, value, firstWcBookResponse) else Future.value(Seq(firstWcBookResponse))
      }
      totalBooks.map(books => {
        val flatttenBooks = books.flatMap(_.toOption).flatten.toList
        Right(WcBookResponse(flatttenBooks))
      })
    })
      .onFailure(ex => {
        logger.error(s"Error occurred while trying to process request : ${ex.getMessage}")
        Future.value(Left(ExternalServiceError(ex.getMessage)))
      })
  }

  private def makeNestedRequest(bookHistoryPath: String, numberOfRequest: Int, firstWcBookResponse: Either[CommonError, List[WcBook]]): Future[Seq[Either[CommonError, List[WcBook]]]] = {
    val range = (1 to numberOfRequest).toList.map(it => makeRateLimitedRequest(Request(s"${bookHistoryPath}&offset=${it * 20}")))
    val futureResponse = Future.collect(range)
    futureResponse.map(nestedResponse => {
      logger.info(s"Response received for makeNestedRequest : ${nestedResponse.size}")
      logger.info(s"Response received for makeNestedRequest : ${nestedResponse.map(_.status).mkString(",")}")
      val nestedContent = nestedResponse.map(_.getContentString())
      val nestedBookResponse = nestedContent.map(parseObj[WebclientResponse](_))
      val nestedBooks = nestedBookResponse.map { nestBookResponse =>
        nestBookResponse.map(nestedItem => {
          nestedItem.results.map(item => {
            val publishedDate = item.ranks_history.headOption.flatMap(_.published_date.map(_.getYear.toString))
            WcBook(item.title, item.author, publishedDate, item.publisher)
          }).toList

        })
      } :+ firstWcBookResponse
      logger.info(s"Response received for makeNestedRequest : ${nestedBooks.size}")
      nestedBooks
    }).onFailure(ex => {
      logger.error(s"Error occurred while trying to process request : ${ex.getMessage}")
      Future.value(Seq(Left(ExternalServiceError(ex.getMessage))))
    })

  }


  val asyncMeter = AsyncMeter.newMeter(1, Duration.fromSeconds(13), 1200)

  /**
   * This method is used to make the rate limited request
   *
   * @param request
   * @return
   */
  def makeRateLimitedRequest(request: Request): Future[Response] = {
    asyncMeter.await(1).flatMap(_ => webClient(request))
  }
}
