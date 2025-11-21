package org.scrapper.multiplatform.action

sealed interface HomeAction {

    data object ProfileBottomSheet : HomeAction

    data object LogoutBottomSheet : HomeAction

    data object Logout : HomeAction

}