package com.amos_tech_code

import com.amos_tech_code.data.Movie
import com.amos_tech_code.data.MovieDataSource
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.exposed.sql.*

fun Application.configureRouting(movieDataSource: MovieDataSource) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("movies") {

            get {
                val movies = movieDataSource.getAllMovies()
                call.respond(movies)
            }

            get("byId") {
                val id = call.queryParameters["id"]?.toIntOrNull() ?: return@get
                val movie = movieDataSource.getMovieById(id)
                call.respond(movie ?: "No movie found with id: $id")
            }

            get("paged") {
                val page = call.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.queryParameters["size"]?.toIntOrNull() ?: 10
                val movies = movieDataSource.getPagedMovie(
                    pageNumber = page,
                    pageSize = size
                )
                call.respond(movies)
            }

            post {
                val movie = call.receive<Movie>()
                movieDataSource.insert(movie)
                call.respond(HttpStatusCode.OK)
            }

            post("insertGetId") {
                val movie = call.receive<Movie>()
                val id = movieDataSource.insertAndGetId(movie)
                call.respond(mapOf("id" to id))
            }

            post("insertIgnore") {
                val movie = call.receive<Movie>()
                val id = movieDataSource.insertIgnore(movie)
                call.respond(HttpStatusCode.OK)
            }

            post("insertIgnoreGetId") {
                val movie = call.receive<Movie>()
                val id = movieDataSource.insertIgnoreGetId(movie)
                call.respond("The inserted row is: $id")
            }

            post("batchInsert") {
                val movies = call.receive<List<Movie>>()
                movieDataSource.batchInsert(movies)
                call.respond(HttpStatusCode.OK)

            }

            patch("update") {
                val movie = call.receive<Movie>()
                movieDataSource.upsertMovie(movie)
                call.respond(HttpStatusCode.OK)
            }

            patch("updateByGenre") {
                val genre = call.queryParameters["genre"]
                val duration = call.queryParameters["duration"]?.toIntOrNull()

                if (genre.isNullOrBlank() || duration == null) {
                    return@patch call.respond(HttpStatusCode.BadRequest)
                }

                movieDataSource.updateMoviesDurationByGenre(duration, genre)
                call.respond(HttpStatusCode.OK)
            }

            delete("deleteMovie") {
                val id = call.queryParameters["id"]?.toIntOrNull() ?:
                return@delete call.respond(HttpStatusCode.BadRequest)

                val deletedCount = movieDataSource.deleteMovieById(id)
                call.respond(message = "Deleted rows: $deletedCount", status = HttpStatusCode.OK)
            }

            delete("deleteMovieByGenre/{genre}") {
                val genre = call.pathParameters["genre"] ?:
                return@delete call.respond(HttpStatusCode.BadRequest)

                val deletedCount = movieDataSource.deleteMovieByGenre(genre)
                call.respond(message = "Deleted rows: $deletedCount", status = HttpStatusCode.OK)

            }

            delete("deleteJoin/{id}") {
                val id = call.pathParameters["id"]?.toIntOrNull() ?:
                return@delete call.respond(HttpStatusCode.BadRequest)

                val deletedCount = movieDataSource.deleteActorsByMovieId(id)
                call.respond(message = "Deleted rows: $deletedCount", status = HttpStatusCode.OK)

            }

            delete("all") {
                val deletedCount = movieDataSource.deleteAllMovies()
                call.respond("Deleted rows: $deletedCount")
            }
        }
    }
}
