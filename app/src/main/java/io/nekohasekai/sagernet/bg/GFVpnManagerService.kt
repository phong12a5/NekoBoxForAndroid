package io.nekohasekai.sagernet.bg

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.aidl.IGFVpnManager
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnMainDispatcher
import io.nekohasekai.sagernet.ktx.snackbar
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import kotlinx.coroutines.delay

class GFVpnManagerService : Service() {
    val TAG = "GFVpnManagerService"
    lateinit var mContext : Context
    private val mIBinder = object : IGFVpnManager.Stub() {
        override fun getVpnStatus(): Int {
            TODO("Not yet implemented")
        }

        override fun getVpnUri(): String {
            TODO("Not yet implemented")
        }

        override fun isReady(): Boolean {
            TODO("Not yet implemented")
        }

        override fun startVpn(uri: String?): Boolean {
            Log.d(TAG, "startVpn: $uri")
            runOnMainDispatcher {
                var existed_proxies = ProfileManager.getAllProfiles()
                for (profile in existed_proxies) {
                    ProfileManager.deleteProfile2(
                        profile.groupId, profile.id
                    )
                }

                val imported_proxies = uri?.let { RawUpdater.parseRaw(it) }
                if (!imported_proxies.isNullOrEmpty()) import(imported_proxies)
//                adapter.selectedGroupIndex = 0
//                adapter?.reload(true)
//                groupPager.setCurrentItem(0, false)
                existed_proxies = ProfileManager.getAllProfiles()
                var profile = existed_proxies[0]
                DataStore.selectedGroup = profile.groupId
                DataStore.selectedProxy = profile.id
                delay(1000)
//                    connect.launch(
//                        null
//                    )

                startActivity( Intent( mContext, VpnRequestActivity::class.java))
            }
            return false
        }

        override fun stopVpn(): Boolean {
            if (DataStore.serviceState.canStop) {
                SagerNet.stopService()
                return true
            }
            return false
        }

    }

    suspend fun import(proxies: List<AbstractBean>) {
        val targetId = DataStore.selectedGroupForImport()
        for (proxy in proxies) {
            ProfileManager.createProfile(targetId, proxy)
        }
        onMainDispatcher {
            DataStore.editingGroup = targetId
        }
    }
    override fun onCreate() {
        super.onCreate()
        mContext = this
        Log.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent): IBinder {
        return mIBinder
    }
}