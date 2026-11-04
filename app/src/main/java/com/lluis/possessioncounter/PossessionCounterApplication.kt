package com.lluis.possessioncounter

import android.app.Application
import com.lluis.possessioncounter.di.AppContainer

class PossessionCounterApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
