package io.nekohasekai.sagernet.aidl;

interface IGFProxyManager {
    // 0: Idle, 1: Connecting, 2: Connected, 3: Stopping, 4: Stopped
    int getProxyStatus();
    String getCurrentProxyUri();
    boolean isReady();
    boolean startProxy(in String uri);
    boolean stopProxy();
}