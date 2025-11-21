package org.scrapper.multiplatform.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.scrapper.multiplatform.dataclass.UserDataClass
import org.scrapper.multiplatform.services.AddResult
import org.scrapper.multiplatform.services.FirebaseServices
import org.scrapper.multiplatform.services.LoginResult
import kotlin.coroutines.resume

class FirebaseRepository(
    private val context: Context
) : FirebaseServices {

    private val firebaseDatabase = Firebase.database("https://autochecker-6955f-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val userRef = firebaseDatabase.getReference("users")

    override suspend fun userLogin(
        userName: String,
        userPassword: String,
        hardwareId: String,
    ): LoginResult {
        return suspendCancellableCoroutine { continuation ->
            val query = userRef.orderByChild("userName").equalTo(userName)
            val userNameListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (continuation.isActive) {
                        if (snapshot.exists()) {
                            val userSnapshot = snapshot.children.first()
                            val userData = userSnapshot.getValue(UserDataClass::class.java)
                            if (userData != null) {
                                if (userData.userPassword == userPassword) {
                                    if (userData.androidId == "" || userData.androidId == hardwareId) {
                                        val update = mapOf(
                                            "androidId" to hardwareId
                                        )
                                        userSnapshot.ref.updateChildren(update)
                                        continuation.resume(LoginResult.Success(userData.userId))
                                    } else {
                                        continuation.resume((LoginResult.HardwareIdConflict))
                                    }
                                } else {
                                    continuation.resume((LoginResult.Fail))
                                }
                            } else {
                                continuation.resume((LoginResult.Fail))
                            }
                        } else {
                            continuation.resume((LoginResult.Fail))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume((LoginResult.Fail))
                }
            }

            query.addListenerForSingleValueEvent(userNameListener)
        }
    }

    override suspend fun getAllUser(): Flow<List<UserDataClass>> {
        return callbackFlow {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userList = mutableListOf<UserDataClass>()
                    if (snapshot.exists()) {
                        for (snapshotChildren in snapshot.children) {
                            val userData = snapshotChildren.getValue(UserDataClass::class.java)
                            if (userData != null) {
                                userList.add(userData)
                            }
                        }
                    }
                    trySend(userList)
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(emptyList())
                }
            }

            userRef.addValueEventListener(listener)

            awaitClose {
                userRef.removeEventListener(listener)
            }
        }
    }

    override suspend fun getUserById(userId: String): Flow<UserDataClass?> {
        return callbackFlow {
            val query = userRef.orderByChild("userId").equalTo(userId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val userData = userSnapshot.getValue(UserDataClass::class.java)
                        if (userData != null) {
                            trySend(userData)
                        }
                    } else {
                        trySend(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(null)
                }
            }

            query.addValueEventListener(listener)

            awaitClose {
                query.removeEventListener(listener)
            }
        }
    }

    override suspend fun addUser(userData: UserDataClass): AddResult {
        return suspendCancellableCoroutine { continuation ->
            val generatedId = userRef.push().key.toString()
            val finalUserData = UserDataClass(
                userId = generatedId,
                userName = userData.userName,
                userPassword = userData.userPassword,
                userRole = userData.userRole
            )

            val detectUsername = userRef.orderByChild("userName").equalTo(userData.userName)
            detectUsername.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (continuation.isActive) continuation.resume((AddResult.UsernameExist))
                    } else {
                        userRef.child(generatedId).setValue(finalUserData)
                            .addOnSuccessListener {
                                if (continuation.isActive) continuation.resume((AddResult.Success))
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume((AddResult.Fail))
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    if (continuation.isActive) continuation.resume((AddResult.Fail))
                }
            })
        }
    }

    override fun deleteHardwareId(userData: UserDataClass) {
        val query = userRef.child(userData.userId)

        val update = mapOf(
            "userId" to userData.userId,
            "userName" to userData.userName,
            "userPassword" to userData.userPassword,
            "userRole" to userData.userRole,
            "androidId" to null
        )
        query.updateChildren(update)
    }

    override fun deleteUser(userData: UserDataClass) {
        userRef.child(userData.userId).removeValue()
    }

}