package com.book.models


import java.time.LocalDate

/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  11:41
 */
object WebClientModels {

  case class ListName(list_name: String)

  case class BookList(list_name: String,published_date:LocalDate, books_details: Seq[WcBook])

  case class BookHistory(title: String,
                         author: String,
                         publisher: String,
                         description: String,
                         price: Int,
                         rank_history: Seq[RankHistory])

  case class WebclientResponse(status: String, num_results: Int, results: Seq[BookHistory])

  case class WcBook(title: String, author: String, maybePublishedYear: Option[LocalDate], publisher: String)

  object WebclientResponse {
    def empty = WebclientResponse("OK", 0,Seq.empty[BookHistory])

  }

  case class RankHistory(rank: Int, weeks_on_list: Int, published_date: LocalDate, bestsellers_date: LocalDate)


}
