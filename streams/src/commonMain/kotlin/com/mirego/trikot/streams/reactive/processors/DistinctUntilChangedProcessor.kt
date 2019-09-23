package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class DistinctUntilChangedProcessor<T>(
    parentPublisher: Publisher<T>,
    name: String? = null
) :
    AbstractProcessor<T, T>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return DistinctUntilChangedProcessorSubscription(subscriber, this)
    }

    class DistinctUntilChangedProcessorSubscription<T>(
        subscriber: Subscriber<in T>,
        publisherDescribable: PublisherDescribable
    ) :
        ProcessorSubscription<T, T>(subscriber, publisherDescribable) {
        private val oldValueReference = AtomicReference<T?>(null)

        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            if (t != oldValueReference.value) {
                oldValueReference.setOrThrow(oldValueReference.value, t)
                subscriber.onNext(t)
            }
        }
    }
}
