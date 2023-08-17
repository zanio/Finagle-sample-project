package com.book.services

import com.book.{CommonError, ExternalServiceError}
import com.book.config.AppConfig
import com.book.models.WebClientModels.{WcBook, WcBookResponse}
import com.book.util.Logger
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}
import com.typesafe.config.Config
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.MockitoSugar
import com.typesafe.config.ConfigFactory

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  23:47
 */
class NyTimesServiceTest extends AnyFlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with MockitoSugar with Logger {

  implicit val config:Config = ConfigFactory.load()

  var mockClient: Service[Request, Response] = mock[Service[Request, Response]]
  var nyTimesService : NyTimesService = _

  override def beforeAll(): Unit = {
    nyTimesService = new NyTimesService(mockClient)
  }

  override def afterAll(): Unit = {
    mockClient.close()
  }


  it should "return books from NyTimesService" in {

    val mockResponse = Response(Status.Ok)
    mockResponse.contentString = """{"results": [{"title": "Book 1", "author": "Author 1"}]}"""
    when(mockClient(any[Request])).thenReturn(Future.value(mockResponse))


    val result: Future[Either[CommonError, WcBookResponse]] = nyTimesService.getBooks("Author 1")

    result.map {
      case Right(response) =>
        response.books should have length 1
        response.books.head.title shouldBe "Book 1"
        response.books.head.author shouldBe "Author 1"
      case Left(_) => fail("Expected Right, got Left")
    }
  }

  it should "return ExternalServiceError for failed request" in {
    when(mockClient(any[Request])).thenReturn(Future.exception(new RuntimeException("Failed request")))

    val nyTimesService = new NyTimesService(mockClient)

    val result: Future[Either[CommonError, WcBookResponse]] = nyTimesService.getBooks("Author 1")

    result.map {
      case Left(error) =>
        error shouldBe a[ExternalServiceError]
        error.asInstanceOf[ExternalServiceError].message shouldBe "Failed request"
      case Right(_) => fail("Expected Left, got Right")
    }
  }

  it should "return ExternalServiceError for invalid JSON content" in {
    val mockResponse = Response(Status.Ok)
    mockResponse.contentString = "Invalid JSON"
    when(mockClient(any[Request])).thenReturn(Future.value(mockResponse))



    val result: Future[Either[CommonError, WcBookResponse]] = nyTimesService.getBooks("Author 1")

    result.map {
      case Left(error) =>
        error shouldBe a[ExternalServiceError]
        error.asInstanceOf[ExternalServiceError].message should include("JSON parsing")
      case Right(_) => fail("Expected Left, got Right")
    }
  }

}
