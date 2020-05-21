package com.mirego.trikot.streams.reactive

import com.mirego.trikot.foundation.CommonJSExport
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

@CommonJSExport
class JustPublisher<T>(private val value: T) : Publisher<T> {
    override fun subscribe(s: Subscriber<in T>) {
        s.onNext(value)
        s.onComplete()
    }
}
