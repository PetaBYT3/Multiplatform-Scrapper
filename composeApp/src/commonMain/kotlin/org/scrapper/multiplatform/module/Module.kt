package org.scrapper.multiplatform.module

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.scrapper.multiplatform.platformFirebaseModule
import org.scrapper.multiplatform.platformSettingsModule
import org.scrapper.multiplatform.repository.SettingsRepository
import org.scrapper.multiplatform.viewmodel.AdminViewModel
import org.scrapper.multiplatform.viewmodel.AppViewModel
import org.scrapper.multiplatform.viewmodel.DptViewModel
import org.scrapper.multiplatform.viewmodel.HomeViewModel
import org.scrapper.multiplatform.viewmodel.LasikViewModel
import org.scrapper.multiplatform.viewmodel.LoginViewModel
import org.scrapper.multiplatform.viewmodel.SiipBpjsViewModel
import org.scrapper.multiplatform.viewmodel.SplashViewModel

object Module {

    private val viewModelModule = module {
        factoryOf(::AppViewModel)
        factoryOf(::SplashViewModel)
        factoryOf(::LoginViewModel)
        factoryOf(::HomeViewModel)
        factoryOf(::AdminViewModel)
        factoryOf(::SiipBpjsViewModel)
        factoryOf(::DptViewModel)
        factoryOf(::LasikViewModel)
    }

    private val repositoryModule = module {
        singleOf(::SettingsRepository)
    }

    private val dependencyModule = module {

    }

    fun getModules() = listOf(
        viewModelModule,
        repositoryModule,
        dependencyModule,
        platformFirebaseModule,
        platformSettingsModule,
    )
}