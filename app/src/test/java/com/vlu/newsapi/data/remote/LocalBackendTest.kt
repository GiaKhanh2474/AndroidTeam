package com.vlu.newsapi.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalBackendTest {
    @Test fun emulatorAndLanNeedLocalNetworkPermission() {
        listOf("10.0.2.2", "192.168.1.10", "172.16.0.1", "172.31.255.1", "127.0.0.1", "dev.local", "[::1]", "[fd00::1]")
            .forEach { assertTrue(it, isLocalBackend("http://$it:5000/api/v1/")) }
    }

    @Test fun publicAndInvalidHostsDoNotPromptForLocalNetwork() {
        listOf("https://news.onrender.com/api/v1/", "http://172.32.0.1/", "http://8.8.8.8/", "http://10.999.1.1/", "invalid", "")
            .forEach { assertFalse(it, isLocalBackend(it)) }
    }
}
