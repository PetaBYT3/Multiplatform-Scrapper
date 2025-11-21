package org.scrapper.multiplatform.route

import kotlinx.serialization.Serializable

@Serializable
sealed class Route {

    @Serializable
    data object SplashPage : Route()

    @Serializable
    data object LoginPage : Route()

    @Serializable
    data object HomePage : Route()

    @Serializable
    data object AdminPage : Route()

    @Serializable
    data object SiipBpjsPage : Route()

    @Serializable
    data object LasikPage : Route()

    @Serializable
    data object DptPage : Route()
}