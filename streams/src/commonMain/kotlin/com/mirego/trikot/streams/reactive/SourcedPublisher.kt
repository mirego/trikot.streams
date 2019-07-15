package com.mirego.trikot.streams.reactive

import com.mirego.trikot.streams.cancellable.CancellableManagerProvider
import com.mirego.trikot.streams.concurrent.AtomicReference
import org.reactivestreams.Publisher

class SourcedPublisher<T>(value: T) : SimplePublisher<T>(value) {
    private val sourceRef = AtomicReference<Publisher<T>?>(null)
    private val cancellableManagerProvider = CancellableManagerProvider()

    var source: Publisher<T>?
        get() = sourceRef.value
        set(value) {
            sourceRef.setOrThrow(sourceRef.value, value)
            subscribeIfNeeded()
        }

    override fun onFirstSubscription() {
        super.onFirstSubscription()
        subscribeIfNeeded()
    }

    override fun onNoSubscription() {
        super.onNoSubscription()
        cancellableManagerProvider.cancelPreviousAndCreate()
    }

    private fun subscribeIfNeeded() {
        if (hasSubscriptions) {
            source?.let { publisher ->
                publisher.subscribe(cancellableManagerProvider.cancelPreviousAndCreate()) {
                    value = it
                }
            }
        }
    }
}
