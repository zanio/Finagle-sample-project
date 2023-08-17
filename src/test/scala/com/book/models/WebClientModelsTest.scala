package com.book.models

import com.book.models.RestModel.ResponseEntity
import io.circe.syntax.EncoderOps
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.decode
import org.scalacheck.Prop.forAll

final class WebClientModelsTest extends AnyFlatSpec with Matchers {

  import com.book.models.WebClientModels._
  import com.book.util.ArbitraryGenerators._

  "WcBook encoder and decoder" should "round-trip correctly" in {
     forAll { (wcBook: WcBook) =>
      decode[WcBook](wcBook.asJson.noSpaces) == Right(wcBook)
    }

  }

  "WcBookResponse encoder and decoder" should "round-trip correctly" in {
    forAll { (wcBookResponse: WcBookResponse) =>
      decode[WcBookResponse](wcBookResponse.asJson.noSpaces) == Right(wcBookResponse)
    }


  }

  "ResponseEntity encoder and decoder" should "round-trip correctly" in {
    forAll { (responseEntity: ResponseEntity) =>
      decode[ResponseEntity](responseEntity.asJson.noSpaces) == Right(responseEntity)
    }


  }



}
