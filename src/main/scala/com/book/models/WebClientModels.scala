package com.book.models


import java.time.LocalDate

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  11:41
 */
object WebClientModels {

  case class WcBook(title: String, author: String, year: LocalDate, publisher: String, genre: String)

  case class ListName(list_name: String)

  case class BookList(list_name: String,published_date:LocalDate, books_details: Seq[WcBook])


}
