package org.scrapper.multiplatform.state

import org.scrapper.multiplatform.dataclass.UserDataClass

data class HomeState(

    val userId: String = "",

    val userData: UserDataClass? = null,

    val profileBottomSheet: Boolean = false,

    val logoutBottomSheet: Boolean = false

)
