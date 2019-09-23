package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

typealias FilterProcessorBlock<T> = (T) -> Boolean

class FilterProcessor<T>(
    parentPublisher: Publisher<T>,
    private val block: FilterProcessorBlock<T>,
    name: String?
) :
    AbstractProcessor<T, T>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return FilterProcessorSubscription(subscriber, block, this)
    }

    class FilterProcessorSubscription<T>(
        subscriber: Subscriber<in T>,
        val block: FilterProcessorBlock<T>,
        publisherDescribable: PublisherDescribable
    ) :
        ProcessorSubscription<T, T>(subscriber, publisherDescribable) {
        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            if (block(t)) subscriber.onNext(t)
        }
    }
}
