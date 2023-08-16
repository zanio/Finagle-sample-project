package com.book.services

import cats.effect.IO
import com.book.models.RestModels.Book
import com.book.models.WebClientModels._
import com.book.util.Logger
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import com.typesafe.config.ConfigObject
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe._
import io.circe.generic.auto._
import io.finch.Error.NotPresent
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  15:45
 */
class NyTimesService(webClient: Service[Request, Response]) extends Logger {
  private val config = com.typesafe.config.ConfigFactory.load()
  private val nyTimesToken = config.getString("nytimes.apiKey")
  private val path = config.getString("nytimes.path")


  def getBooks: Future[Seq[WcBook]] = {
    val bookHistoryPath = path + s"?api-key=$nyTimesToken"
    val request = Request(s"${bookHistoryPath}")
    logger.info(s"Request received for getBooks : ${request}")
    request.method(com.twitter.finagle.http.Method.Get)
    val response = webClient(request)
    response.flatMap(resp => {
      val content = resp.getContentString()
      logger.info(s"Response received for getBooks : ${content}")
      val bookResponse = io.circe.parser.decode[WebclientResponse](content).getOrElse(WebclientResponse.empty)
      logger.info(s"Response received for getBooks : ${bookResponse.results}")
      val books = bookResponse.results.map(item => {
        val maybePublishedDate = item.rank_history.headOption.map(_.published_date)
        WcBook(item.title, item.author, maybePublishedDate,item.publisher)
      })
      Future.value(books)
    })
      .onFailure(ex => {
        logger.error(s"Error in getBooks : ${ex.getMessage}")
        Future.exception(ex)
      })
  }

}
