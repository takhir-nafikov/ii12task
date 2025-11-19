package org.ufku.ii12task

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform