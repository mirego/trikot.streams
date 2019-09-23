package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

typealias MapProcessorBlock<T, R> = (T) -> R

class MapProcessor<T, R>(
    parentPublisher: Publisher<T>,
    private val block: MapProcessorBlock<T, R>,
    name: String? = null
) :
    AbstractProcessor<T, R>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in R>): ProcessorSubscription<T, R> {
        return MapProcessorSubscription(subscriber, block, this)
    }

    class MapProcessorSubscription<T, R>(
        subscriber: Subscriber<in R>,
        private val block: MapProcessorBlock<T, R>,
        publisherDescribable: PublisherDescribable
    ) : ProcessorSubscription<T, R>(subscriber, publisherDescribable) {
        override fun onNext(t: T, subscriber: Subscriber<in R>) {
            subscriber.onNext(block(t))
        }
    }
}
