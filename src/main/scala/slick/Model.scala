package slick

import slick.SlickTables.movieActorMappingTable
import slick.lifted.ProvenShape

import java.time.LocalDate

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)

case class Actor(id: Long, name: String)

case class MovieActorMapping(id: Long, movieId: Long, actorId: Long) // for joining

case class StreamingProviderMapping(id: Long, movieId: Long, streamingProvider: StreamingService.Provider)

object StreamingService extends Enumeration {
  type Provider = Value
  val First = Value("First")
  val Second = Value("Second")
  val Third = Value("Third")
}

object SlickTables {

  import slick.jdbc.PostgresProfile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def releaseDate = column[LocalDate]("release_date")

    def lengthInMin = column[Int]("length_in_min")

    // Mapping function to a case class
    override def * : ProvenShape[Movie] = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)

  }

  lazy val movieTable = TableQuery[MovieTable]

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    override def * : ProvenShape[Actor] = (id, name) <> (Actor.tupled, Actor.unapply)
  }

  lazy val actorTable = TableQuery[ActorTable]

  class MovieActorMappingTable(tag: Tag) extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") {
    def id = column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)

    def movieId = column[Long]("movie_id")

    def actorId = column[Long]("actor_id")

    override def * : ProvenShape[MovieActorMapping] = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }

  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]

  class StreamingProviderMappingTable(tag: Tag) extends Table[StreamingProviderMapping](tag, Some("movies"), "StreamingProviderMapping") {
    implicit val providerMapper = MappedColumnType.base[StreamingService.Provider, String](
      provider => provider.toString,
      string => StreamingService.withName(string)
    )

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def movieId = column[Long]("movie_id")

    def streamingProvider = column[StreamingService.Provider]("streaming_provider")

    override def * : ProvenShape[StreamingProviderMapping] = (id, movieId, streamingProvider) <> (StreamingProviderMapping.tupled, StreamingProviderMapping.unapply)
  }

  lazy val streamingProviderMappingTable = TableQuery[StreamingProviderMappingTable]

  // table generation scripts
  val tables = List(movieTable, actorTable, movieActorMappingTable, streamingProviderMappingTable)
  val ddl = tables.map(_.schema).reduce(_ ++ _)
}

object TableDefinitionGenerator {
  def main(args: Array[String]): Unit = {
    println(SlickTables.ddl.createIfNotExistsStatements.mkString(";\n"))
  }
}
