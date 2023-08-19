package com.book.clients
import com.book.config.AppConfig
import com.book.util.Logger
import com.twitter.conversions.DurationOps.richDurationFromInt
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.redis.Client
import com.twitter.finagle.{Http, Redis, Service, SimpleFilter}
import com.twitter.util.Future
object ClientSetUp  extends Logger with AppConfig{

  private def logFilter(message: String): SimpleFilter[Request, Response] = (request: Request, service: Service[Request, Response]) => {
    if(request.isRequest){
      logger.info(s"$message : ${request.uri}")
    } else logger.info(s"$message : ${request.contentLength}")

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
      .withRequestTimeout(10.second)
      .filtered(logFilter("Sending request to web client"))
      .withHttp2
      .withTransport.tls(destination)
      .newService(webClientConnectionString)
}
