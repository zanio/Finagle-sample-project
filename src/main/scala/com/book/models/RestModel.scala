package com.book.models


object RestModel {
  import WebClientModels._
  import io.circe.{Decoder, Encoder, HCursor, Json}
  case class ResponseEntity(status: Int, message: String, result: List[WcBook])

  object ResponseEntity {
    def empty: ResponseEntity = ResponseEntity(200, "", List.empty[WcBook])

    def success(data: List[WcBook]): ResponseEntity = ResponseEntity(200, "Book record successfully retrieved", data)

    def failure400(message: String): ResponseEntity = ResponseEntity(400, message, List.empty[WcBook])
    def failure404(message: String): ResponseEntity = ResponseEntity(404, message, List.empty[WcBook])

    def failure500(message: String): ResponseEntity = ResponseEntity(500, message, List.empty[WcBook])
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
