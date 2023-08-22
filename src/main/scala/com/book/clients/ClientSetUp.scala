package com.book.clients

import com.book.{CommonError, RedisConnectionError}
import com.book.config.AppConfig
import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.finagle.{Http, Redis, Service, SimpleFilter}
import com.twitter.util.Future

import java.net.{InetSocketAddress, Socket}

object ClientSetUp extends Logger with AppConfig {

  private def logFilter(message: String): SimpleFilter[Request, Response] = (request: Request, service: Service[Request, Response]) => {
    if (request.isRequest) {
      logger.info(s"$message : ${request.uri}")
    } else logger.info(s"$message : ${request.contentLength}")

    service(request)
  }

  def isRedisReachable(timeoutMillis: Int): Either[RedisConnectionError, Boolean] = {
    try {
      val socket = new Socket()
      val address = new InetSocketAddress(redisHost, redisPort)
      socket.connect(address, timeoutMillis)
      socket.close()
      logger.info("Redis is reachable")
      Right(true)
    } catch {
      case e: Throwable => logger.error("Not reachable"); Left(RedisConnectionError(e.getMessage))
    }
  }

  def redisClient: Client = {

    val redisClient = Redis.newRichClient(redisConnectionString)
    redisClient.auth(com.twitter.io.Buf.Utf8(redisPassword))
    redisClient
  }

  case class RedisCache() {
    val cache = redisClient

    lazy val close: Future[Unit] = cache.close()
    def get(cacheKey: String): Either[CommonError,Future[Option[com.twitter.io.Buf]]] = {
      val cacheKeyBuf = com.twitter.io.Buf.Utf8(cacheKey)
      isRedisReachable(1000) match {
        case Right(_) => Right(cache.get(cacheKeyBuf))
        case Left(error) => Left(error)
      }
    }

    def set(cacheKey: String, cacheValue: String): Either[CommonError, Future[Unit]] = {
      val cacheKeyBuf = com.twitter.io.Buf.Utf8(cacheKey)
      val cacheValueBuf = com.twitter.io.Buf.Utf8(cacheValue)
      isRedisReachable(1000) match {
        case Right(_) => Right(cache.setEx(cacheKeyBuf, REDIS_TTL.seconds.inLongSeconds, cacheValueBuf))
        case Left(error) =>  Left(error)
      }
    }
  }

  def makeWebClient: Service[Request, Response] =
    Http.client
      .withLabel(clientLabel)
      .withRequestTimeout(10.second)
      .filtered(logFilter("Sending request to web client"))
      .withHttp2
      .withTransport.tls(destination)
      .newService(webClientConnectionString)
}
