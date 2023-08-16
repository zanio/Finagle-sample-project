package com.book

import java.time.LocalDate
import cats.effect.IO
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

case class Book(
                 book_id: String,
                 title: String,
                 author: String,
                 published_date: LocalDate
               )
object Main extends App {
  val basePath = "api" :: "v1"

  val webClient = makeWebClient
  val nyTimesService = new NyTimesService(webClient)
  val apis = new RestApi(nyTimesService).endpoints
  val redisConnector = redisClient
  val cacheFilter = new ResponseCachingFilter(redisConnector)

  def service: Service[Request, Response] = cacheFilter andThen(Bootstrap
    .serve[Application.Json](apis)
    .toService)

  Await.ready(Http.server.serve(":8081", service))
}


