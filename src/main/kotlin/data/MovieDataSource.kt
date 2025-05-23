package com.amos_tech_code.data

import com.amos_tech_code.data.MovieDataSource.Movies.description
import com.amos_tech_code.data.MovieDataSource.Movies.durationInMinutes
import com.amos_tech_code.data.MovieDataSource.Movies.genre
import com.amos_tech_code.data.MovieDataSource.Movies.tags
import com.amos_tech_code.data.MovieDataSource.Movies.title
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.concat
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class MovieDataSource(private val database: Database) {

    init {
        transaction(database) {
            SchemaUtils.create(Movies, Actors)
            //Insertion in table Actors
//            Actors.insert {
//                it[movieId] = EntityID(10, Movies)
//                it[name] = "Actor 1"
//            }
//            Actors.insert {
//                it[movieId] = EntityID(10, Movies)
//                it[name] = "Actor 2"
//            }
//            Actors.insert {
//                it[movieId] = EntityID(10, Movies)
//                it[name] = "Actor 3"
//            }
//            Actors.insert {
//                it[movieId] = EntityID(15, Movies)
//                it[name] = "Actor 4"
//            }
        }
    }

    object Movies : IntIdTable() {
        val title = varchar("title", 100)
        val genre = varchar("genre", 100)
        val description = text("description")
        val durationInMinutes = integer("duration_in_minute")
        val tags = array("tags", columnType = VarCharColumnType(70))
    }

    object Actors : IntIdTable() {
        val movieId = reference("movie_id", Movies)
        val name = varchar("name", 100)
    }


    // ----------- Write Operations --------------
    fun insert(movie: Movie) {
        transaction(database) {
            Movies.insert {
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }
        }
    }

    fun insertAndGetId(movie: Movie) : Int {
        return transaction(database) {
            Movies.insertAndGetId {
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }.value
        }
    }

    fun insertIgnore(movie: Movie) {
        transaction(database) {
            Movies.insertIgnore {
                if (movie.id != null) {
                    it[id] = movie.id
                }
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }
        }
    }

    fun insertIgnoreGetId(movie: Movie) : Int? {
        return transaction(database) {
            Movies.insertIgnoreAndGetId {
                if (movie.id != null) {
                    it[id] = movie.id
                }
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }?.value
        }
    }

    fun batchInsert(movies: List<Movie>) {
        transaction(database) {
            Movies.batchInsert(movies) { movie ->
                this[title] = movie.title
                this[description] = movie.description
                this[genre] = movie.genre
                this[tags] = movie.tags
                this[durationInMinutes] = movie.duration
            }
        }
    }

    // ------------- Read Operations --------------
    fun getAllMovies() : List<Movie> {
        return transaction(database) {
            Movies.selectAll().toList().map {
                it.toMovie()
            }
        }
    }

    fun getMovieById(id: Int) : Movie? {
        return transaction(database) {
            Movies.selectAll().where {
                Movies.id eq id
            }.firstOrNull()?.toMovie()
        }
    }

    fun getMoviesNotInGenre() {}

    //Pagination
    fun getPagedMovie(pageNumber: Int, pageSize: Int = 10) : List<Movie> =
        transaction(database) {
            val offset = ((pageNumber - 1) * pageSize).toLong()

            Movies.selectAll().limit(pageSize).offset(offset).toList().map { it.toMovie() }
        }

    // ------------------------ Update Operations -------------------

    fun updateMovieById(movie: Movie) {
        if (movie.id == null) return
        transaction(database) {
            Movies.update(
                where = { Movies.id eq movie.id }
            ) {
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }
        }
    }

    fun upsertMovie(movie: Movie) {
        if (movie.id == null) return
        transaction(database) {
            Movies.upsert(
                onUpdateExclude = listOf(tags, description),
                onUpdate = {
                    it[genre] = concat(insertValue(genre), stringLiteral("|"))
                    it[description] = movie.description
                }
            ) {
                it[id] = movie.id
                it[title] = movie.title
                it[description] = movie.description
                it[genre] = movie.genre
                it[tags] = movie.tags
                it[durationInMinutes] = movie.duration
            }
        }

    }

    fun updateMoviesDurationByGenre(
        duration: Int,
        genre: String
    ) {
        transaction(database) {
            Movies.update(
                where = { Movies.genre eq genre }
            ) {
                it[durationInMinutes] = duration
            }
        }
    }


    // ------------------- Delete Operations ----------------------------

    fun deleteMovieById(id: Int) : Int = transaction(database) {
        Movies.deleteWhere {
            Movies.id eq id
        }
    }

    fun deleteMovieByGenre(genre: String) : Int = transaction(database) {
        Movies.deleteWhere {
            Movies.genre eq genre
        }
    }

    // Delete operation using JOIN
    fun deleteActorsByMovieId(id: Int) : Int = transaction(database) {
        val join = Movies.join(
            Actors,
            JoinType.INNER,
            additionalConstraint = { Movies.id eq Actors.movieId }
        )
        join.delete(Actors) {
            Movies.id eq id
        }

    }

    fun deleteAllMovies() : Int = transaction(database) {
        Movies.deleteAll()
    }



    private fun ResultRow.toMovie() : Movie {
        return Movie(
            id = this[Movies.id].value,
            title = this[title],
            genre = this[genre],
            description = this[description],
            duration = this[durationInMinutes],
            tags = this[tags]
        )
    }

}

@Serializable
data class Movie(
    val id: Int? = null,
    val title: String,
    val genre: String,
    val description: String,
    val duration: Int,
    val tags: List<String>
)