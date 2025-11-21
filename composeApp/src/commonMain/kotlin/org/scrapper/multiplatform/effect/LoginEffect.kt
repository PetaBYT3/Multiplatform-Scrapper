package org.scrapper.multiplatform.effect

import org.scrapper.multiplatform.route.Route

interface LoginEffect {

    data class Navigate(val route: Route) : LoginEffect

}