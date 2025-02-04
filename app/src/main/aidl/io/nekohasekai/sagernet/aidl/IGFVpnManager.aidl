package io.nekohasekai.sagernet.aidl;

interface IGFVpnManager {
    // 0: disconnected, 1: connecting, 2: connected
    int getVpnStatus();
    String getVpnUri();
    boolean isReady();
    boolean startVpn(in String uri);
    boolean stopVpn();
}