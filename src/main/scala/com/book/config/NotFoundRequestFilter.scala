package com.book.config

import com.book.models.RestModel.ResponseEntity
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.circe.syntax.EncoderOps


class NotFoundRequestFilter(response: Response) extends SimpleFilter[Request, Response]{
    def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      service(request).map {
        case rep if rep.status == Status.NotFound =>
          response.contentString = ResponseEntity.failure404("Not Found").asJson.noSpaces
          response
        case rep => rep
      }
    }
}
