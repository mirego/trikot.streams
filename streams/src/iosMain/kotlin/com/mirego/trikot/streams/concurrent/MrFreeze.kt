package com.mirego.trikot.streams.concurrent

import kotlin.native.concurrent.freeze

actual object MrFreeze {
    actual fun <T> freeze(objectToFreeze: T): T {
        return objectToFreeze.freeze()
    }
}

actual fun <T> freeze(objectToFreeze: T): T {
    return MrFreeze.freeze(objectToFreeze)
}
