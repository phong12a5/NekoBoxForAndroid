package io.nekohasekai.sagernet.bg

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.aidl.IGFProxyManager
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnMainDispatcher
import io.nekohasekai.sagernet.ktx.snackbar
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import kotlinx.coroutines.delay

class GFProxyManagerService : Service(), SagerConnection.Callback {
    val TAG = "GFVpnManagerService"
    lateinit var mContext : Context

    private val connection = SagerConnection(SagerConnection.CONNECTION_ID_GFPROXY_MGR_SERVICE)

    private val mIBinder = object : IGFProxyManager.Stub() {
        override fun getProxyStatus(): Int {
            return proxyState.ordinal
        }

        override fun getCurrentProxyUri(): String? {
            var existed_proxies = ProfileManager.getAllProfiles()
            if (!existed_proxies.isNullOrEmpty()) {
                var profile = existed_proxies[0]
                return profile.toStdLink()
            }
            return null
        }

        override fun isReady(): Boolean {
            return android.net.VpnService.prepare(mContext) == null
        }

        override fun startProxy(uri: String?): Boolean {
            runOnMainDispatcher {
                var existed = false;
                var existedProxies = ProfileManager.getAllProfiles()
                for (profile in existedProxies) {
                    if (profile.toStdLink().startsWith(uri!!)) {
                        existed = true
                        DataStore.selectedGroup = profile.groupId
                        DataStore.selectedProxy = profile.id
                        continue
                    }

                    ProfileManager.deleteProfile2(
                        profile.groupId, profile.id
                    )
                }

                if (!existed) {
                    val importedProxies = uri?.let { RawUpdater.parseRaw(it) }
                    if (!importedProxies.isNullOrEmpty()) import(importedProxies)
                    existedProxies = ProfileManager.getAllProfiles()
                    var profile = existedProxies[0]
                    DataStore.selectedGroup = profile.groupId
                    DataStore.selectedProxy = profile.id
                    delay(1000)
                }

                if (android.net.VpnService.prepare(mContext) != null) {
                    startActivity(
                        Intent( mContext, VpnRequestActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } else {
                    SagerNet.startService()
                }
            }
            return false
        }

        override fun stopProxy(): Boolean {
            if (proxyState.canStop) {
                SagerNet.stopService()
                return true
            }
            return false
        }

    }

    private var proxyState = BaseService.State.Idle

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
        connection.connect(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconnect(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return mIBinder
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        proxyState = state
    }

    override fun onServiceConnected(service: ISagerNetService) {
        proxyState = BaseService.State.values()[service.state]
    }
}