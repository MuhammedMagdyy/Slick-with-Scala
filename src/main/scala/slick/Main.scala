package slick

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure, Try}

object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutorService(executor)

}

object Main {

  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val myLife = Movie(1L, "My Life", LocalDate.of(2001, 3, 1), 100)
  val anotherLife = Movie(2L, "Another Life", LocalDate.of(2002, 3, 1), 160)

  def insertMovie(): Unit = {
    val queryDescription = SlickTables.movieTable += anotherLife
    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(movieId) => println(s"Ok, new id is $movieId")
      case Failure(ex) => println(s"Failed, reason $ex")
    }

    Thread.sleep(10000)
  }

  def getAllMovies(): Unit = {
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result) // same as SELECT * FROM table

    resultFuture.onComplete {
      case Success(movies) => println(s"Movies retrieved ${movies.mkString(",")}")
      case Failure(ex) => println(s"Reason $ex")
    }

    Thread.sleep(10000)
  }

  def getMovie(): Unit = {
    // SELECT * FROM table WHERE name LIKE "My Life"
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%My Life%")).result)

    resultFuture.onComplete {
      case Success(movie) => println(s"Movie retrieved ${movie.mkString(",")}")
      case Failure(ex) => println(s"Reason $ex")
    }

    Thread.sleep(10000)
  }

  def updateMovie(): Unit = {
    val resultFuture = SlickTables.movieTable.filter(_.id === 1L).update(myLife.copy(lengthInMin = 150))
    val futureId: Future[Int] = Connection.db.run(resultFuture)

    futureId.onComplete {
      case Success(movie) => println(s"Movie updated $movie")
      case Failure(ex) => println(s"Reason $ex")
    }

    Thread.sleep(10000)
  }

  def deleteMovie(): Unit = {
    Connection.db.run(SlickTables.movieTable.filter(_.id === 2L).delete)

    Thread.sleep(10000)
  }

  def main(args: Array[String]): Unit = {
    deleteMovie()
  }
}
