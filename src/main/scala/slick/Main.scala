package slick

import slick.jdbc.GetResult

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext {
  val executor = Executors.newFixedThreadPool(4)
  implicit val ex: ExecutionContext = ExecutionContext.fromExecutorService(executor)

}

object Main {

  import slick.jdbc.PostgresProfile.api._
  import PrivateExecutionContext._

  val myLife = Movie(1L, "My Life", LocalDate.of(2001, 3, 1), 100)
  val anotherLife = Movie(2L, "Another Life", LocalDate.of(2002, 3, 1), 160)
  val anotherLifeQube = Movie(3L, "Another Life Qube", LocalDate.of(2003, 3, 1), 150)

  val mohamedMagdy = Actor(1L, "Mohamed Magdy")
  val anaWadGamed = Actor(2L, "Ana Wad Gamed")
  val anaT3ban5ales = Actor(3L, "Ana T3ban 5ales")

  val providers = List(
    StreamingProviderMapping(0L, 1L, StreamingService.First),
    StreamingProviderMapping(1L, 2L, StreamingService.Second),
    StreamingProviderMapping(2L, 3L, StreamingService.Third),
  )

  def insertMovie(): Unit = {
    val queryDescription = SlickTables.movieTable += anotherLife
    val futureId: Future[Int] = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(movieId) => println(s"Ok, new id is $movieId")
      case Failure(ex) => println(s"Failed, reason $ex")
    }

    Thread.sleep(10000)
  }

  def insertActor(): Unit = {
    val queryDescription = SlickTables.actorTable ++= Seq(mohamedMagdy, anaWadGamed) // adds multiple actors
    val futureId = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(actorId) => println(s"Ok, inserted with id $actorId")
      case Failure(ex) => println(s"Reason: $ex")
    }
  }

  def addStreamingProviders() = {
    val insertQuery = SlickTables.streamingProviderMappingTable ++= providers
    val futureId = Connection.db.run(insertQuery)

    futureId.onComplete {
      case Success(streamProviders) => streamProviders.foreach(println)
      case Failure(ex) => println(s"Reason $ex")
    }
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

  def getMoviesByPlainQuery(): Future[Vector[Movie]] = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(position => Movie(
        position.<<,
        position.<<,
        LocalDate.parse(position.nextString()),
        position.<<
      ))
    val query = sql"""SELECT * FROM movies."Movie"""".as[Movie]
    Connection.db.run(query)
  }

  def multipleQuerySingleTransaction() = {
    val movieQuery = SlickTables.movieTable += anotherLifeQube
    val actorQuery = SlickTables.actorTable += anaT3ban5ales
    val insertIntoDB = DBIO.seq(movieQuery, actorQuery)

    Connection.db.run(insertIntoDB.transactionally) // transactionally means if one fails then roll back
  }

  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.movieId === movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id)
      .map(_._2)

    Connection.db.run(joinQuery.result)
  }

  def findProvidersForMovie(movieId: Long): Future[Seq[StreamingProviderMapping]] = {
    val query = SlickTables.streamingProviderMappingTable
      .filter(_.movieId === movieId)

    Connection.db.run(query.result)
  }

  def main(args: Array[String]): Unit = {
    //    insertMovie()
    //    getMoviesByPlainQuery().onComplete {
    //      case Success(movies) => movies.foreach(println)
    //      case Failure(ex) => println(s"Reason: $ex")
    //    }
    //    insertActor()
    //    multipleQuerySingleTransaction()
    //    findAllActorsByMovie(2).onComplete {
    //      case Success(actors) => actors.foreach(println)
    //      case Failure(ex) => println(s"Reason $ex")
    //    }
    //    addStreamingProviders()
    findProvidersForMovie(2).onComplete {
      case Success(movies) => movies.foreach(println)
      case Failure(exception) => println(s"Cause $exception")
    }

    Thread.sleep(5000)
    PrivateExecutionContext.executor.shutdown()
  }
}
