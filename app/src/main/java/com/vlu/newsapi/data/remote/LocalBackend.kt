package com.vlu.newsapi.data.remote

import java.net.URI

/** No DNS lookup: only development hosts that explicitly name a local network. */
internal fun isLocalBackend(baseUrl: String): Boolean {
    val host = runCatching { URI(baseUrl).host?.lowercase() }.getOrNull() ?: return false
    if (host == "localhost" || host.endsWith(".localhost") || host.endsWith(".local")) return true
    val ipv6 = host.removePrefix("[").removeSuffix("]")
    if (ipv6 == "::1" || (ipv6.contains(':') &&
        (ipv6.startsWith("fc") || ipv6.startsWith("fd") || ipv6.startsWith("fe80:")))) return true
    val parts = host.split('.').map { it.toIntOrNull() ?: return false }
    if (parts.size != 4 || parts.any { it !in 0..255 }) return false
    return parts[0] == 10 || parts[0] == 127 ||
        (parts[0] == 192 && parts[1] == 168) ||
        (parts[0] == 172 && parts[1] in 16..31) ||
        (parts[0] == 169 && parts[1] == 254)
}
