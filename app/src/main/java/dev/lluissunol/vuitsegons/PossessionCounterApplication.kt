package dev.lluissunol.vuitsegons

import android.app.Application
import dev.lluissunol.vuitsegons.di.AppContainer

class PossessionCounterApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
