package com.book.models

import io.circe.syntax.EncoderOps
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.decode
import org.scalacheck.Prop.forAll
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  06:34
 */
class WebClientModelsTest extends AnyFlatSpec with Matchers {

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




}
