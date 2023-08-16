package com.book

import com.book.config.AppConfig.{makeWebClient, redisClient}
import com.book.config.ResponseCachingFilter
import com.book.services.NyTimesService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

object IngRestApp extends App {

  val webClient = makeWebClient
  val nyTimesService = new NyTimesService(webClient)
  val apis = new RestApi(nyTimesService).endpoints
  val redisConnector = redisClient
  val cacheFilter = new ResponseCachingFilter(redisConnector)

  def service: Service[Request, Response] = cacheFilter andThen(Bootstrap
    .serve[Application.Json](apis)
    .toService)

  Await.ready(Http.serve(":8081", service))
}


