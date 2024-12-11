package com.example.myapplication

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User

    @Query("UPDATE user_table SET password = :password, age = :age, postcode =:postcode, city =:city WHERE username = :username")
    suspend fun updateUser(username: String, password: String, age: Int, postcode: Int, city: String)

}
