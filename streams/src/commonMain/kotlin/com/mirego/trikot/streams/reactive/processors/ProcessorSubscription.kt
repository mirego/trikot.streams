package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.streams.reactive.PublisherDescribable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

abstract class ProcessorSubscription<T, R>(private val subscriber: Subscriber<in R>, private val publisherDescribable: PublisherDescribable) :
    Subscriber<T>, PublisherDescribable {
    val activeSubscription = AtomicReference<Subscription>(object :
        Subscription {
        override fun cancel() {
        }

        override fun request(n: Long) {
        }
    })

    override val name: String = publisherDescribable.name

    override fun onNext(t: T) {
        onNext(t, subscriber)
    }

    abstract fun onNext(t: T, subscriber: Subscriber<in R>)

    override fun onError(t: Throwable) {
        subscriber.onError(t)
    }

    override fun onComplete() {
        subscriber.onComplete()
    }

    protected fun cancelActiveSubscription() {
        activeSubscription.value.cancel()
    }

    open fun onCancel(s: Subscription) {
        s.cancel()
    }

    override fun onSubscribe(s: Subscription) {
        val subscription = object : Subscription {
            override fun request(n: Long) {
                s.request(n)
            }

            override fun cancel() {
                onCancel(s)
            }
        }
        activeSubscription.setOrThrow(activeSubscription.value, subscription)
        subscriber.onSubscribe(subscription)
    }

    override fun describeProperties(): Map<String, Any?> {
        return publisherDescribable.describeProperties()
    }
}
