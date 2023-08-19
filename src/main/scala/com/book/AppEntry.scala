package com.book

import com.book.clients.ClientSetUp
import com.book.config.{AppConfig, NotFoundRequestFilter, ResponseCachingFilter}
import com.book.services.NyTimesService
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Duration}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._


object AppEntry extends TwitterServer with AppConfig {

  val webClient: Service[Request, Response] = ClientSetUp.makeWebClient
  val nyTimesService = new NyTimesService(webClient)
  private val apis = new RestApi(nyTimesService).endpoints
  private val redisConnector = ClientSetUp.redisClient
  private val cacheFilter = new ResponseCachingFilter(redisConnector)
  private val notFoundFilter = new NotFoundRequestFilter(Response(Status.NotFound))

  def service: Service[Request, Response] =  notFoundFilter andThen cacheFilter andThen Bootstrap
    .serve[Application.Json](apis)
    .toService

  def main(): Unit = {
    val server = Http.server
      .withLabel("ING_Assessment")
      .withRequestTimeout(Duration.fromMinutes(20))
      .serve(":8081", service)

    onExit {
      server.close()
      Await.result(server)
      Await.result(redisConnector.close())
      Await.result(webClient.close())
    }
    Await.ready(server)
  }
}


