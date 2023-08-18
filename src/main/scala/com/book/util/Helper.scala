package com.book.util

import com.book.{CommonError, ParsingError}
import io.circe.parser.decode
import io.circe.Decoder
object Helper {

  /**
   * parse the json string to the given object
   * @param content
   * @param decoder
   * @tparam T
   * @return
   */
  def parseObj[T](content: String)(implicit decoder: Decoder[T]): Either[CommonError, T] = {
    decode[T](content) match {
      case Left(error) => Left(ParsingError(error.getMessage))
      case Right(value) => Right(value)
    }
  }

  /**
   * check if the year is valid, I'm using 1900 as the minimum year, because I don't think we have books before that year
   * @param year
   * @return
   */
  def isValidYear(year: String): Boolean = {
    val currentYear = java.time.LocalDate.now.getYear
    (year.nonEmpty && year.length == 4) &&
    (scala.util.Try(year.toInt).isSuccess && (year.toInt >= 1900 && year.toInt <= currentYear))
  }

  def isValidYears(years: String): Boolean = {
      years.split(",").toList match {
        case yearList if years.nonEmpty =>
          yearList.forall(isValidYear)
        case _ => true
      }
  }
}
