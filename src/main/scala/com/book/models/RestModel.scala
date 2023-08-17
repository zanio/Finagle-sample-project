package com.book.models

import com.book.models.WebClientModels.{WcBookResponse}

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  13:10
 */
object RestModel {
  import io.circe.{Decoder, Encoder, HCursor, Json}
  import WebClientModels._
  case class ResponseEntity(status: Int, message: String, result: List[WcBook])

  object ResponseEntity {
    def empty = ResponseEntity(200, "", List.empty[WcBook])

    def success(data: List[WcBook]) = ResponseEntity(200, "Book record successfully retrieved", data)

    def failure400(message: String) = ResponseEntity(400, message, List.empty[WcBook])

    def failure500(message: String) = ResponseEntity(500, message, List.empty[WcBook])
  }

  implicit val decodeResponseEntity: Decoder[ResponseEntity] = (c: HCursor) => for {
    status <- c.downField("status").as[Int]
    message <- c.downField("message").as[String]
    result <- c.downField("result").as[List[WcBook]]
  } yield {
    ResponseEntity(status, message, result)
  }

  implicit val encodeResponseEntity: Encoder[ResponseEntity] = (a: ResponseEntity) => Json.obj(
    ("status", Json.fromInt(a.status)),
    ("message", Json.fromString(a.message)),
    ("result", Json.fromValues(a.result.map(WebClientModels.encodeWcBook.apply)))
  )
}
