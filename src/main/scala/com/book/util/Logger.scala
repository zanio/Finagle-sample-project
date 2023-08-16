package com.book.util

import org.slf4j.LoggerFactory

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  13:32
 */
trait Logger {
  /**
   * The logger instance. to use for logging.
   */
  val logger = LoggerFactory.getLogger(getClass)
}
