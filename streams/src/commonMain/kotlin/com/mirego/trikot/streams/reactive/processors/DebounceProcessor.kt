package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.timers.PlatformTimer
import com.mirego.trikot.streams.cancellable.CancellableManagerProvider
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class DebounceProcessor<T>(parentPublisher: Publisher<T>, private val delayMs: Duration) :
    AbstractProcessor<T, T>(parentPublisher) {
    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return DebounceProcessorSubscription(subscriber, delayMs)
    }

    class DebounceProcessorSubscription<T> constructor(
        subscriber: Subscriber<in T>,
        private val delayMs: Duration
    ) : ProcessorSubscription<T, T>(subscriber) {
        private val cancellableManagerProvider = CancellableManagerProvider()

        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            cancellableManagerProvider.cancelPreviousAndCreate().also { cancellableManager ->
                PlatformTimer(delayMs, false) { subscriber.onNext(t) }
                    .also {
                        cancellableManager.add { it.cancel() }
                    }
            }
        }

        override fun onCancel(s: Subscription) {
            super.onCancel(s)
            cancellableManagerProvider.cancelPreviousAndCreate()
        }
    }
}
