package org.scrapper.multiplatform.services

import kotlinx.coroutines.flow.Flow
import org.scrapper.multiplatform.dataclass.UserDataClass

interface FirebaseServices {

    suspend fun userLogin(userName: String, userPassword: String, hardwareId: String): LoginResult

    suspend fun getAllUser() : Flow<List<UserDataClass>>

    suspend fun getUserById(userId: String): Flow<UserDataClass?>

    suspend fun addUser(userData: UserDataClass) : AddResult

    fun deleteHardwareId(userData: UserDataClass)

    fun deleteUser(userData: UserDataClass)

}