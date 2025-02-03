package io.nekohasekai.sagernet.bg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import io.nekohasekai.sagernet.aidl.IGFVpnManager;

public class GFVpnManagerService extends Service {
    IGFVpnManager.Stub mBinder = new IGFVpnManager.Stub() {
        @Override
        public boolean startVpn(String uri) throws RemoteException {
            return false;
        }

        @Override
        public boolean stopVpn() throws RemoteException {
            return false;
        }

        @Override
        public int getVpnStatus() throws RemoteException {
            return 0;
        }

        @Override
        public String getVpnUri() throws RemoteException {
            return null;
        }
    };

    public GFVpnManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}