package com.book

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  11:12
 */

import com.book.models.WebClientModels.WcBookResponse
import com.book.services.NyTimesService
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RestApiTest extends AnyFlatSpec with Matchers with MockitoSugar {

  val mockNyTimesService = mock[NyTimesService]
  when(mockNyTimesService.getBooks("Author1")).thenReturn(Future.value(Right(WcBookResponse.empty)))
  when(mockNyTimesService.getBooks("InvalidAuthor")).thenReturn(Future.value(Left( InvalidInput("Invalid author"))))

  val restApi = new RestApi(mockNyTimesService)

  // Set up Finch server
  private val apis = restApi.endpoints
  private val service: Service[Request, Response] = Bootstrap
    .serve[Application.Json](apis)
    .toService

  it should "return WcBookResponse for valid fetchBooks request" in {
    val request = Request("/me/books/list?author=Author1&year=2022")
    val response = Await.result(service(request))
    response.status shouldBe com.twitter.finagle.http.Status.Ok

    response.contentString should(
      include(""""result":[]""")
      )
  }

  it should "return BadRequest for missing author and year" in {
    val request = Request("/me/books/list")
    val response = Await.result(service(request))
    response.status shouldBe Status.BadRequest
  }

  it should "return BadRequest for missing author" in {
    val request = Request("/me/books/list?year=2022")
    val response = Await.result(service(request))
    response.status shouldBe Status.BadRequest
  }

  it should "return BadRequest for invalid year" in {
    val request = Request("/me/books/list?author=Author1&year=invalid")
    val response = Await.result(service(request))
    response.status shouldBe Status.BadRequest
  }


}
