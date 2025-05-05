package com.amos_tech_code

import com.amos_tech_code.data.BookDataSource
import com.amos_tech_code.data.DatabaseFactory
import com.amos_tech_code.data.MovieDataSource
import com.amos_tech_code.data.UserDataSource
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val databaseFactory = DatabaseFactory()
    val userDataSource = UserDataSource(databaseFactory.database)
    val bookDataSource = BookDataSource(databaseFactory.database)
    val movieDataSource = MovieDataSource(databaseFactory.database)

    configureSerialization()
    configureRouting(movieDataSource)

}
