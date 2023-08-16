package com.book.config

import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.util.Future

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  16:32
 */
class ResponseCachingFilter(cache: Client) extends SimpleFilter[Request, Response] with Logger{

  def generateCacheKey(request: Request): String = {
    request.path
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val cacheKey = generateCacheKey(request)
    val cacheKeyBuf = com.twitter.io.Buf.Utf8(cacheKey)
    val checkKey = cache.get(cacheKeyBuf)

    checkKey.flatMap {
      case Some(value) =>
        logger.info(s"Cache hit for key: $cacheKey")
        val convertValue = com.twitter.io.Buf.Utf8.unapply(value) match {
          case Some(value) => value
          case _ => ""
        }
        val response = Response(request.version, com.twitter.finagle.http.Status.Ok)
        response.contentString = convertValue
        Future.value(response)
      case None =>
        logger.info(s"Cache miss for key: $cacheKey")
        service(request).flatMap { response =>
          if (response.status.code == 200 && response.contentString.nonEmpty && request.path == "") {
            logger.info(s"Setting cache for key: $cacheKey")
            cache.setEx(cacheKeyBuf, 180.seconds.inLongSeconds, com.twitter.io.Buf.Utf8(response.contentString))
          }
          // filter
          Future.value(response)
        }
    }


  }

}

