package com.book

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  12:01
 */
/**
 * The parent error from which most API errors extend. Thrown whenever something in the api goes wrong.
 */
abstract class CommonError(msg: String)  extends Exception(msg) {
  def message: String
}

/**
 * Thrown when the object given is invalid
 * @param message An error message
 */
case class InvalidInput(message: String) extends CommonError(message)

/**
 * Thrown when the object given is invalid. This should be used in the context of an external service
 * @param message
 */
case class ExternalServiceError(message: String) extends CommonError(message)


case class ParsingError(message: String) extends CommonError(message)

case class RedisConnectionError(message: String) extends CommonError(message)
