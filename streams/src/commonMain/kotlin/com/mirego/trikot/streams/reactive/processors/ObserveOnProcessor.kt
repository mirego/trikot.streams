package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.dispatchQueue.DispatchQueue
import com.mirego.trikot.foundation.concurrent.dispatchQueue.QueueDispatcher
import com.mirego.trikot.foundation.concurrent.dispatchQueue.SequentialDispatchQueue
import com.mirego.trikot.foundation.concurrent.dispatchQueue.dispatch
import com.mirego.trikot.foundation.concurrent.freeze
import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

open class ObserveOnProcessor<T>(
    parentPublisher: Publisher<T>,
    dispatchQueue: DispatchQueue,
    name: String? = null
) : AbstractProcessor<T, T>(parentPublisher, name) {

    private val dispatchQueue = when {
        dispatchQueue.isSerial() -> dispatchQueue
        else -> SequentialDispatchQueue(dispatchQueue)
    }

    override fun createSubscription(subscriber: Subscriber<in T>): ProcessorSubscription<T, T> {
        return ObserveOnProcessorSubscription(subscriber, dispatchQueue, this)
    }

    class ObserveOnProcessorSubscription<T>(
        s: Subscriber<in T>,
        override val dispatchQueue: DispatchQueue,
        publisherDescribable: PublisherDescribable
    ) : ProcessorSubscription<T, T>(s, publisherDescribable), QueueDispatcher {

        override fun onNext(t: T, subscriber: Subscriber<in T>) {
            freeze(t)
            dispatch { subscriber.onNext(t) }
        }

        override fun onError(t: Throwable) {
            freeze(t)
            dispatch { super.onError(t) }
        }

        override fun onComplete() {
            dispatch { super.onComplete() }
        }
    }
}
