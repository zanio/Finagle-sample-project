package com.book.util

import com.book.models.RestModel.ResponseEntity


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
      data <- Gen.containerOfN[List, WcBook](10,arbWcBook.arbitrary)
    } yield WcBookResponse(data)
  }

  implicit val arbResponseEntity: Arbitrary[ResponseEntity] = Arbitrary {
    for {
      status <- Gen.choose(200, 500)
      message <- Gen.alphaStr
      data <- arbWcBookResponse.arbitrary
    } yield ResponseEntity(status, message, data.books.size, data.books)
  }

}
