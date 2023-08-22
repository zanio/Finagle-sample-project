package com.book.config

import com.book.CommonError
import com.book.clients.ClientSetUp.RedisCache
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
import io.circe.syntax.EncoderOps

/**
 * This filter is responsible for caching the response from the web client when the request path is = /me/books/list
 * @param cache
 */
class ResponseCachingFilter(cache: RedisCache) extends SimpleFilter[Request, Response] with Logger with AppConfig {
  import RestModel._


  private def generateCacheKey(request: Request): String = {
    val author = request.getParam("author")
    logger.info(s"Generating cache key for request: ${request.path}:$author")
    s"${request.path}:$author"
  }

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    if(request.path == "/me/books/list"){
      logger.info(s"Request path: ${request.path}")
      handleCache(request, service)
    } else {
      service(request)
    }
  }

  private def handleCache(request: Request, service: Service[Request, Response]) = {
    val cacheKey = generateCacheKey(request)
    logger.info(s"Cache key: $cacheKey")
    val eitherCacheKey: Either[CommonError, Future[Option[Buf]]] = cache.get(cacheKey)
    val filterResponseByDate = request.getParam("year", "")
    val author = request.getParam("author")
    val cacheResponse = Response(request.version, com.twitter.finagle.http.Status.Ok)

    if (author == null || author.isEmpty) {
      proceedWithRequest(request, service, cacheKey, cacheResponse, filterResponseByDate)
    } else if (filterResponseByDate.nonEmpty && !isValidYears(filterResponseByDate)) {
      logger.info(s"Invalid year passed in request: $filterResponseByDate")
      proceedWithRequest(request, service, cacheKey, cacheResponse, filterResponseByDate)
    } else {
      eitherCacheKey match {
        case Left(value) =>
          logger.error(s"Error occurred while trying to get cache: $value")
          proceedWithRequest(request, service, cacheKey, cacheResponse, filterResponseByDate)
        case Right(value) =>
          value.flatMap {
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
              proceedWithRequest(request, service, cacheKey, cacheResponse, filterResponseByDate)
          }.onFailure(ex => logger.error(s"Error occurred while trying to get cache: $ex"))
      }
    }
  }

  private def proceedWithRequest(request: Request,
                                 service: Service[Request, Response],
                                 cacheKey: String,
                                 cacheResponse: Response,
                                 filterResponseByDate: String) = {
    service(request).map { response =>
      if (response.status.code == 200 && response.getContentString().nonEmpty && request.path.contains("books")) {
        val resp = decodeResponse(response.getContentString())

        logger.info(s"Setting cache for key: $cacheKey")
        cache.set(cacheKey, resp.asJson(encodeResponseEntity).noSpaces)
        cacheResponse.contentString = filterResponse(filterResponseByDate, resp.result).asJson(encodeResponseEntity).noSpaces
        cacheResponse
      } else {
        response
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
    val years = filterResponseByDate.split(",").toList
    if (filterResponseByDate.nonEmpty) {
      val filteredDataWcBookResponse = data.filter(book => {
        years.exists(it => book.year.contains(it))}
      )
      ResponseEntity.success(filteredDataWcBookResponse)
    } else {
      ResponseEntity.success(data)
    }
  }
}

