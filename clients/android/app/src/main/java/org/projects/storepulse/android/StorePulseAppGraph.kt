package org.projects.storepulse.android

import android.content.Context
import org.projects.storepulse.android.data.DataStoreSessionStore
import org.projects.storepulse.android.data.SessionStore
import org.projects.storepulse.android.data.StorePulseGateway
import org.projects.storepulse.android.data.StorePulseRepository
import org.projects.storepulse.android.ui.StorePulseViewModel

object StorePulseAppGraph {
    @Volatile
    var factory: ((Context) -> StorePulseViewModel)? = null

    fun createViewModel(context: Context): StorePulseViewModel {
        val override = factory
        if (override != null) {
            return override(context)
        }

        val repository: StorePulseGateway = StorePulseRepository(BuildConfig.API_BASE_URL)
        val sessionStore: SessionStore = DataStoreSessionStore(context.applicationContext)
        return StorePulseViewModel(repository, sessionStore)
    }

    fun reset() {
        factory = null
    }
}
