package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Processor
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

abstract class AbstractProcessor<T, R>(
    val parentPublisher: Publisher<T>,
    describableName: String? = null
) :
    Processor<T, R>, PublisherDescribable {

    override val name: String = describableName?.let { it } ?: this::class.toString()

    abstract fun createSubscription(subscriber: Subscriber<in R>): ProcessorSubscription<T, R>

    override fun subscribe(s: Subscriber<in R>) {
        parentPublisher.subscribe(createSubscription(s))
    }

    private fun cancelActiveSubscription() {
    }

    override fun onSubscribe(s: Subscription) {
    }

    override fun onError(t: Throwable) {
    }

    override fun onComplete() {
    }

    override fun onNext(t: T) {
    }

    override fun describeProperties(): Map<String, Any?> {
        return mapOf("parentPublisher" to parentPublisher)
    }
}
