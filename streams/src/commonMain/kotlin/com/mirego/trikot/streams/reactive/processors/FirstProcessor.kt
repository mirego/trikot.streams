package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class FirstProcessor<T>(parentPublisher: Publisher<T>, name: String? = null) :
    AbstractProcessor<T, T>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return FirstProcessorSubscription(subscriber, this)
    }

    class FirstProcessorSubscription<T>(s: Subscriber<in T>, publisherDescribable: PublisherDescribable) : ProcessorSubscription<T, T>(s, publisherDescribable) {

        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            if (t != null) {
                subscriber.onNext(t)
                subscriber.onComplete()
                cancelActiveSubscription()
            }
        }
    }
}
