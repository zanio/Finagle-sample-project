package com.book.services

import com.book.config.AppConfig
import com.book.models.WebClientModels.WcBook
import com.book.util.Logger
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  23:47
 */
class NyTimesServiceTest extends AnyFlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with Logger {

  var client: Service[Request, Response] = _
  var nyTimesService : NyTimesService = _

  override def beforeAll(): Unit = {
    client = AppConfig.makeWebClient
    nyTimesService = new NyTimesService(client)

  }

  override def afterAll(): Unit = {
    client.close()
  }

  "NyTimesService.getListNames" should "assert nonEmpty" in {
    val response: Future[Seq[WcBook]] = nyTimesService.getBooks
    Await.result(response.map(resp => {
      logger.debug(s"getListNames :: getListNames : ${resp}")
      assert(resp.nonEmpty)
    }))
  }


}
