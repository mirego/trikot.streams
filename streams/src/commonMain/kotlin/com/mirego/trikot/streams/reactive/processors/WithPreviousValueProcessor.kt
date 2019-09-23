package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class WithPreviousValueProcessor<T>(
    parentPublisher: Publisher<T>,
    name: String? = null
) :
    AbstractProcessor<T, Pair<T?, T>>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in Pair<T?, T>>): ProcessorSubscription<T, Pair<T?, T>> {
        return WithPreviousValueProcessorSubscription(subscriber, this)
    }

    class WithPreviousValueProcessorSubscription<T>(
        subscriber: Subscriber<in Pair<T?, T>>,
        publisherDescribable: PublisherDescribable
    ) :
        ProcessorSubscription<T, Pair<T?, T>>(subscriber, publisherDescribable) {
        private val oldValueReference = AtomicReference<T?>(null)

        override fun onNext(t: T, subscriber: Subscriber<in Pair<T?, T>>) {
            val previousValue = oldValueReference.value
            oldValueReference.setOrThrow(oldValueReference.value, t)
            subscriber.onNext(previousValue to t)
        }
    }
}
