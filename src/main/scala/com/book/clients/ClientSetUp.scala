package com.book.clients
import com.book.config.AppConfig
import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.finagle.{Http, Redis, Service, SimpleFilter}
object ClientSetUp  extends Logger with AppConfig{

  private val logFilter: SimpleFilter[Request, Response] = (request: Request, service: Service[Request, Response]) => {
    logger.info(s"Starting : ${request}")
    service(request)
  }
  def redisClient: Client = {
    val redisClient = Redis.newRichClient(redisConnectionString)
    redisClient.auth(com.twitter.io.Buf.Utf8(redisPassword))
    redisClient
  }

  def makeWebClient: Service[Request, Response] =
    Http.client
      .withLabel(clientLabel)
      .withRequestTimeout(6.second)
      .filtered(logFilter)
      .withSessionPool.maxSize(1)
      .withTransport.tls(destination)
      .newService(webClientConnectionString)
}
