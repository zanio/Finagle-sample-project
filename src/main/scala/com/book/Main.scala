package com.book

import cats.effect.{IO, IOApp}
import com.book.config.AppConfig.{logFilter, makeWebClient, redisClient}
import com.book.config.ResponseCachingFilter
import com.book.services.NyTimesService
import com.book.util.Logger
import com.twitter.util.Future
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.finch.internal.currentTime

import java.util.{Date, Locale}


object Main extends IOApp.Simple with Endpoint.Module[IO] with Logger {
  case class Locale(language: String, country: String)

  case class Time(locale: Locale, time: String)

  val time: Endpoint[IO,Time] =
    post("time" :: jsonBody[Locale]) { l: Locale =>
      IO(Ok(Time(l, currentTime())))
    }

  override def run: IO[Unit] = {
    val webClient = makeWebClient
    val nyTimesService = new NyTimesService(webClient)
    val apis = new Endpoints(nyTimesService).endpoints
    val redisConnector = redisClient
    val cacheFilter = new ResponseCachingFilter(redisConnector)

    Bootstrap[IO]
      .filter(logFilter)
      .filter(cacheFilter)
      .serve[Application.Json](apis)
      .listen(":8081")
      .useForever
  }
}


