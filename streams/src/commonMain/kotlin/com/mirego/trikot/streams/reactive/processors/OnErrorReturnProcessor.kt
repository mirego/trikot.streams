package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

typealias OnErrorReturnProcessorBlock<T> = (Throwable) -> T

class OnErrorReturnProcessor<T>(
    parentPublisher: Publisher<T>,
    private val block: OnErrorReturnProcessorBlock<T>,
    name: String? = null
) :
    AbstractProcessor<T, T>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return OnErrorReturnSubscription(subscriber, block, this)
    }

    class OnErrorReturnSubscription<T>(
        private val subscriber: Subscriber<in T>,
        private val block: OnErrorReturnProcessorBlock<T>,
        publisherDescribable: PublisherDescribable
    ) : ProcessorSubscription<T, T>(subscriber, publisherDescribable) {

        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            subscriber.onNext(t)
        }

        override fun onError(t: Throwable) {
            subscriber.onNext(block(t))
        }
    }
}
