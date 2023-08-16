package com.book.config

import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.finagle.{Http, Redis, Service, SimpleFilter}
import com.twitter.util.Future
import com.typesafe.config.ConfigObject

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  13:56
 */
object AppConfig extends Logger {
  private val config = com.typesafe.config.ConfigFactory.load()

  private val destination: String = config.getString("nytimes.host")
  private val clientLabel = config.getString("nytimes.clientLabel")
  private val nyTimesToken = config.getString("nytimes.apiKey")

  private val redisHost = config.getString("redis.host")
  private val redisPort = config.getInt("redis.port")
  private val redisPassword = config.getString("redis.password")
  private val redisConnectionString = s"$redisHost:$redisPort"

  private val webClientConnectionString = s"$destination:433"


  val logFilter: SimpleFilter[Request, Response] = (request: Request, service: Service[Request, Response]) => {
    logger.info(s"Starting : ${request}")
    service(request)
  }

  def redisClient: Client =  {
    val redisClient = Redis.newRichClient(redisConnectionString)
    redisClient .auth(com.twitter.io.Buf.Utf8(redisPassword))
    redisClient
  }

  private val TokenHeaderFilter = new SimpleFilter[Request, Response] {
    def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      request.headerMap.add("api-key", s"$nyTimesToken") // Add the token header
      service(request)
    }
  }

  def makeWebClient : Service[Request, Response] =
    Http.client.withLabel(clientLabel)
           .withRequestTimeout(1.second)
           .filtered(logFilter)
           .filtered(TokenHeaderFilter)
           .withSessionPool.maxSize(1)
           .newService(webClientConnectionString)
}


