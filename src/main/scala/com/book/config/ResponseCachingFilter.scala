package com.book.config

import com.book.models.RestModel
import com.book.models.WebClientModels.WcBook
import com.book.util.Helper._
import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.io.Buf
import com.twitter.util.Future
import com.typesafe.config.Config
import io.circe.syntax.EncoderOps
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  16:32
 */
class ResponseCachingFilter(cache: Client)(implicit val config: Config) extends SimpleFilter[Request, Response] with Logger{
  import RestModel._

    private val REDIS_TTL = config.getInt("redis.ttl")

  def generateCacheKey(request: Request): String = {
    val author = request.getParam("author")
    logger.info(s"Generating cache key for request: ${request.path}:$author")
    s"${request.path}:$author"

  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val cacheKey = generateCacheKey(request)
    val cacheKeyBuf = com.twitter.io.Buf.Utf8(cacheKey)
    val checkKey = cache.get(cacheKeyBuf)
    val filterResponseByDate = request.getParam("year", "")
    val author = request.getParam("author")
    val cacheResponse = Response(request.version, com.twitter.finagle.http.Status.Ok)

    if(author == null || author.isEmpty){
      return  proceedWithRequest(request, service, cacheKey, cacheKeyBuf, cacheResponse, filterResponseByDate)
    }
    if(filterResponseByDate.nonEmpty && !isValidYear(filterResponseByDate)){
      logger.info(s"Invalid year passed in request: $filterResponseByDate")
      return  proceedWithRequest(request, service, cacheKey, cacheKeyBuf, cacheResponse, filterResponseByDate)
    }

    checkKey.flatMap {
      case Some(value) =>
        logger.info(s"Cache hit for key: $cacheKey")
        val decodedListWcBook = com.twitter.io.Buf.Utf8.unapply(value) match {
          case Some(redisValue) => decodeResponse(redisValue)
        }
        cacheResponse.contentString = filterResponse(filterResponseByDate,
          decodedListWcBook.result).asJson(encodeResponseEntity).noSpaces
        Future.value(cacheResponse)
      case None =>
        logger.info(s"Cache miss for key: $cacheKey")
        proceedWithRequest(request, service, cacheKey, cacheKeyBuf, cacheResponse, filterResponseByDate)
    }

  }

  private def proceedWithRequest(request: Request,
                                 service: Service[Request, Response],
                                 cacheKey: String, cacheKeyBuf: Buf,
                                 cacheResponse: Response,
                                 filterResponseByDate: String) = {
    service(request).flatMap { response =>
      if (response.status.code == 200 && response.getContentString().nonEmpty && request.path.contains("books")) {
        val resp = decodeResponse(response.getContentString())

        logger.info(s"Setting cache for key: $cacheKey")
        cache.setEx(cacheKeyBuf, REDIS_TTL.seconds.inLongSeconds,
          com.twitter.io.Buf.Utf8(resp.asJson(encodeResponseEntity).noSpaces))
        cacheResponse.contentString = filterResponse(filterResponseByDate, resp.result).asJson(encodeResponseEntity).noSpaces
        Future.value(cacheResponse)
      } else {
        Future.value(response)
      }
    }
  }

  private def decodeResponse(value: String) = {
    io.circe.parser.decode[ResponseEntity](value) match {
      case Right(value) => value
      case Left(ex) =>
        logger.error(s"Error occurred while trying to decode: $ex")
        ResponseEntity.empty
    }
  }

  private def filterResponse(filterResponseByDate: String, data: List[WcBook]):ResponseEntity = {
    if (filterResponseByDate.nonEmpty) {
      val filteredDataWcBookResponse = data.filter(_.year.contains(filterResponseByDate))
      ResponseEntity.success(filteredDataWcBookResponse)
    } else {
      ResponseEntity.success(data)
    }
  }
}

