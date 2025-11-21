package org.scrapper.multiplatform.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class SettingsRepository(
    private val observableSettings: ObservableSettings
) {

    //User Session
    private val userId = "userId"
    fun getUserId(): Flow<String> {
        return observableSettings.getStringFlow(userId, "")
    }
    fun setUserId(newUserId: String) {
        observableSettings.putString(userId, newUserId)
    }

    //Settings
    private val getGmail = "getGmail"
    private val getYmail = "getYmail"

    fun getGmailOption(): Flow<Boolean> {
        return observableSettings.getBooleanFlow(getGmail, true)
    }
    fun setGmailOption(newGmailOption: Boolean) {
        observableSettings.putBoolean(getGmail, newGmailOption)
    }

    fun getYmailOption(): Flow<Boolean> {
        return observableSettings.getBooleanFlow(getYmail, true)
    }
    fun setYmailOption(newYmailOption: Boolean) {
        observableSettings.putBoolean(getYmail, newYmailOption)
    }
}