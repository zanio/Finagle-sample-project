package com.book.util


/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  06:58
 */
object ArbitraryGenerators {

  import org.scalacheck.Arbitrary
  import org.scalacheck.Gen
  import org.scalacheck.Gen._
  import com.book.models.WebClientModels._

  implicit val arbWcBook: Arbitrary[WcBook] = Arbitrary {
    for {
      title <- Gen.alphaStr
      author <- Gen.alphaStr
      year <- Gen.option(Gen.choose(1900, 2021))
      publisher <- Gen.option(Gen.alphaStr)
    } yield WcBook(title, author, year.map(_.toString), publisher)
  }

  implicit val arbWcBookResponse: Arbitrary[WcBookResponse] = Arbitrary {
    for {
      data <- Gen.containerOfN[Seq, WcBook](10,arbWcBook.arbitrary)
    } yield WcBookResponse(data)
  }

}
