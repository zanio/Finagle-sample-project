package com.book

import com.book.clients.ClientSetUp
import com.book.config.{AppConfig, ResponseCachingFilter}
import com.book.services.NyTimesService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._


object AppEntry extends TwitterServer with AppConfig {

  val webClient: Service[Request, Response] = ClientSetUp.makeWebClient
  val nyTimesService = new NyTimesService(webClient)
  private val apis = new RestApi(nyTimesService).endpoints
  private val redisConnector = ClientSetUp.redisClient
  private val cacheFilter = new ResponseCachingFilter(redisConnector)

  def service: Service[Request, Response] = cacheFilter andThen  Bootstrap
    .serve[Application.Json](apis)
    .toService

  def main(): Unit = {
    val server = Http.server.serve(":8081", service)
    onExit {
      server.close()
    }
    Await.ready(server)
  }
}


