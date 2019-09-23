package com.mirego.trikot.streams.reactive.helpers

import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class SubscriberMock<T>: Subscriber<T> {
    var subscription: Subscription? = null
    var lastValue: T? = null
    var error: Throwable? = null
    var completed: Boolean = false
    override fun onSubscribe(s: Subscription) {
        subscription = s
    }

    override fun onNext(t: T) {
        lastValue = t
    }

    override fun onError(t: Throwable) {
        error = t
    }

    override fun onComplete() {
        completed = true
    }

    val describable: PublisherDescribable? get() {
        return subscription as? PublisherDescribable
    }
}
