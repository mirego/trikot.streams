package com.mirego.trikot.streams.reactive

import com.mirego.trikot.foundation.CommonJSExport

@CommonJSExport
interface BehaviorSubject<T> : MutablePublisher<T> {
    override var value: T?
    override var error: Throwable?
    override fun complete()
}
