package com.book.services

import cats.effect.IO
import com.book.models.RestModels.Book
import com.book.models.WebClientModels._
import com.book.util.Logger
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import com.typesafe.config.ConfigObject
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
/**
 * Project working on ing_assessment
 * New File created by ani in  ing_assessment @ 15/08/2023  15:45
 */
class NyTimesService(webClient: Service[Request, Response]) extends Logger {
  private val config = com.typesafe.config.ConfigFactory.load()

  val path: ConfigObject = config.getObject("nytimes.path")


  def getListNames: Future[List[ListName]] = {
    val namesPath = path.get("names").unwrapped().toString
    val request = Request(namesPath)
    logger.info(s"Request received for getListNames : ${request}")
    request.method(com.twitter.finagle.http.Method.Get)
    val response = webClient(request)
//    val ioResponse: IO[com.twitter.finagle.http.Response] = IO.fromFuture(IO(response))

    response.flatMap(resp => {
      val content = resp.getContentString()
      val listNames = io.circe.parser.decode[List[ListName]](content).getOrElse(List.empty[ListName])
      Future.value(listNames)
    })
  }

  def getBooksForListName(name: ListName): Future[List[Book]] = {
    val booksPath = path.get("books").unwrapped().toString
    val request = Request(s"$booksPath/${name.list_name}")
    logger.info(s"Request received for getBooks : ${request}")
    request.method(com.twitter.finagle.http.Method.Get)
    val response = webClient(request)
    response.flatMap(resp => {
      val content = resp.getContentString()
      val bookList = io.circe.parser.decode[List[BookList]](content).getOrElse(List.empty[BookList])

      val books = bookList.flatMap(book => {
        val bookD = book.books_details.map(bookDetails => {
          Book(bookDetails.title, bookDetails.author, bookDetails.year, bookDetails.publisher)
        })
        bookD
      })

      Future.value(books)
    })
  }

 def fetchSetup(): Future[Seq[Book]] = {
   val listNamesFuture = getListNames
   listNamesFuture.flatMap { listNames =>
     val concurrentCalls = 3
     val booksFutures = listNames.map { listName =>
       getBooksForListName(listName)
     }
     booksFutures.grouped(concurrentCalls).foldLeft(Future.value(Seq.empty[Book])) {
       case (accumulatedFuture, group) =>
         val groupFuture = Future.collect(group)
         accumulatedFuture.flatMap { accumulatedResponses =>
           groupFuture.map(groupResponses => accumulatedResponses ++ groupResponses.flatten)
         }
     }

   }
 }



}
