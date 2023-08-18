package com.book.util

import com.book.models.WebClientModels.WcBook
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

final class HelperTest extends AnyFlatSpec with Matchers {

  it should ("parseObj when given valid json") in {
    val wcBookJson = """{"title":"War and Peace","author":"Leo Tolstoy","year":"1869"}"""
    Helper.parseObj[WcBook](wcBookJson) match {
      case Right(result) =>
        assert(result.title == "War and Peace")
        assert(result.author == "Leo Tolstoy")
      case Left(_) => fail("parseObj failed")
    }
  }

  it should "parseObj when given invalid json" in {
    val wcBookJson = """{"title":"War and Peace","author":"Leo Tolstoy","year":"1869""""

      Helper.parseObj[WcBook](wcBookJson)
      match {
        case Right(_) => fail("parseObj should have failed")
        case Left(_) => succeed
      }

  }

  it should "isValidYear when given valid year" in {
    assert(Helper.isValidYear("2021"))
  }

  it should "assert when given invalid year" in {
    assert(!Helper.isValidYear("20211"))
    assert(!Helper.isValidYear("202"))
    assert(!Helper.isValidYear("202a"))
  }

  it should "assert isValidYears when given valid years" in {
    assert(Helper.isValidYears("2021,2022"))
  }

  it should "assert isValidYears when given invalid years" in {
    assert(!Helper.isValidYears("2021,2022x"))
  }

  it should "assert isValidYears when given year without value" in {
    assert(Helper.isValidYears(""))
  }



}
