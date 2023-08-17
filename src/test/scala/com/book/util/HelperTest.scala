package com.book.util

import com.book.models.WebClientModels.WcBook
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 17/08/2023  05:19
 */
class HelperTest extends AnyFlatSpec with Matchers {

  it should ("parseObj when given valid json") in {
    val wcBookJson = """{"title":"War and Peace","author":"Leo Tolstoy","year":"1869"}"""
    val result = Helper.parseObj[WcBook](wcBookJson)
    assert(result.title == "War and Peace")
    assert(result.author == "Leo Tolstoy")
  }

  it should "parseObj when given invalid json" in {
    val wcBookJson = """{"title":"War and Peace","author":"Leo Tolstoy","year":"1869""""
    intercept[io.circe.ParsingFailure] {
      Helper.parseObj[WcBook](wcBookJson)
    }

  }

  it should "isValidYear when given valid year" in {
    assert(Helper.isValidYear("2021"))
  }

  it should "isValidYear when given invalid year" in {
    assert(!Helper.isValidYear("20211"))
    assert(!Helper.isValidYear("202"))
    assert(!Helper.isValidYear("202a"))
  }

}
