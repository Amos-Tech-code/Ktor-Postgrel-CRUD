package com.amos_tech_code.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class UserDataSource(database: Database) {

    object Users : Table("users") {
        private val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)

        //Numerical Types
        val age = integer("age")
        val heightInCm = short("height_in_cm")
        val followerCount = long("follower_count")
        val rating = float("rating")
        val accountBalance = decimal("account_balance", 12, 2)

        //Boolean
        val isActive = bool("is_active").default(false)
        //String
        val gender = char("gender", 1)
        val email = varchar("email", 80)
        val bio = text("bio").nullable()

        //Array Data Type
        val tags = array<String>("tags").default(emptyList())
        val skills = array<String?>("skills", columnType = VarCharColumnType(50))
        val doublesColumn = array("doubles_column", columnType = DoubleColumnType()).nullable()

        val array2D = array<Int, List<List<Int>>>("array2D", dimensions = 2)
        val array3D = array<String, List<List<List<String>>>>("array3D", dimensions = 3)

        override val primaryKey = PrimaryKey(id)
    }

//    init {
//        transaction(database) {
//            SchemaUtils.create(Users)
//            Users.insert {
//                it[name] = "Test1"
//                it[age] = 25
//                it[heightInCm] = 175
//                it[followerCount] = 100_000
//                it[rating] = 4.77f
//                it[accountBalance] = BigDecimal("12345.567")
//               // it[isActive] = true
//                it[gender] = "M"
//                it[email] = "test@example.com"
//                //it[bio] = "This is some random shit"
//                it[tags] = listOf("tag1","tag2","tag3")
//                it[skills] = listOf("Kotlin","ktor","Postgres")
//                it[doublesColumn] = listOf(3.5,2.5,1.8)
//
//                it[array2D] = listOf(
//                    listOf(1,2),
//                    listOf(3,4)
//                )
//
//                it[array3D] = listOf(
//                    listOf(
//                        listOf("a","b"),
//                        listOf("c","d"),
//                    ),
//                    listOf(
//                        listOf("e","f"),
//                        listOf("g","h"),
//                    )
//                )
//            }
//        }
//    }
}