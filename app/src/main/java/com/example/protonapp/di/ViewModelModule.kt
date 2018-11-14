package com.example.protonapp.di

import androidx.lifecycle.ViewModelProvider
import com.example.protonapp.viewmodel.ViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val viewModelModule = Kodein.Module {
    bind<ViewModelProvider.Factory>() with singleton { ViewModelFactory(kodein) }
}
