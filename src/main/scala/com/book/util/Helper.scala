package com.book.util
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 16/08/2023  22:42
 */
object Helper {

  /**
   * parse the json string to the given object
   * @param content
   * @param decoder
   * @tparam T
   * @return
   */
  def parseObj[T](content: String)(implicit decoder: io.circe.Decoder[T]): T = {
    io.circe.parser.decode[T](content) match {
      case Left(error) => throw error
      case Right(value) => value
    }
  }

  /**
   * check if the year is valid, I'm using 1900 as the minimum year, because I don't think we have books before that
   * @param year
   * @return
   */
  def isValidYear(year: String): Boolean = {
    val currentYear = java.time.LocalDate.now.getYear
    (year.nonEmpty && year.length == 4) &&
    (scala.util.Try(year.toInt).isSuccess && (year.toInt >= 1900 && year.toInt <= currentYear))
  }
}
