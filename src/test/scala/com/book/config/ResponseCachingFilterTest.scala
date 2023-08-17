package com.book.config

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.redis.Client
import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  11:43
 */

class ResponseCachingFilterTest extends AnyFlatSpec with Matchers with MockitoSugar {
  implicit val config:Config = ConfigFactory.load()
  it should "return cached response for cache hit" in {

    val mockCache = mock[Client]
    val mockService = mock[Service[Request, Response]]

      // my json structure has changed, so I have to change the cacheHitJson
    // ResponseEntity
    val cacheHitJson = makeResponEntityJson

    when(mockCache.get(any())).thenReturn(Future.value(Some(Buf.Utf8(cacheHitJson))))

    val cachingFilter = new ResponseCachingFilter(mockCache)

    val request = Request("/?author=Author1")
    val response: Response = Await.result(cachingFilter(request, mockService))

    verify(mockCache).get(any())

    response.status shouldBe Status.Ok
    response.getContentString() should(
      include("Book 1") and
        include("Author 1") and
        include("2020") and
        include("Addison-Wesley Professional"))
  }

  it should "return original response for cache miss" in {
    val mockCache = mock[Client]
    val mockService = mock[Service[Request, Response]]

    when(mockCache.get(any())).thenReturn(Future.value(None))

    val mockResponse = Response(Status.Ok)
    mockResponse.contentString = makeResponEntityJson
    when(mockService(any())).thenReturn(Future.value(mockResponse))

    val cachingFilter = new ResponseCachingFilter(mockCache)

    val request = Request("/?author=Author1")

    val response: Response = Await.result(cachingFilter(request, mockService))

    verify(mockCache).get(any())

    verify(mockService).apply(any())

    response.status shouldBe Status.Ok
    response.getContentString() should(
      include("Book 1") and
        include("Author 1") and
        include("2020") and
        include("Addison-Wesley Professional"))
  }

  def makeResponEntityJson: String = {
    """{
      |  "status": "200",
      |  "message": "Book record successfully retrieved",
      |  "result": [
      |      {
      |        "title": "Book 1",
      |        "author": "Author 1",
      |        "year": "2020",
      |        "publisher": "Addison-Wesley Professional"
      |      },
      |      {
      |        "title": "Book 2",
      |        "author": "Author 1",
      |        "year": "2020",
      |        "publisher": "Addison-Wesley Professional"
      |      }
      |    ]
      |}""".stripMargin
  }


}
