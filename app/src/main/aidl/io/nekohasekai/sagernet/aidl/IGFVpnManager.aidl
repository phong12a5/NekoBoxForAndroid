package io.nekohasekai.sagernet.aidl;

interface IGFVpnManager {
    boolean startVpn(in String uri);
    boolean stopVpn();
    // 0: disconnected, 1: connecting, 2: connected
    int getVpnStatus();
    String getVpnUri();
}