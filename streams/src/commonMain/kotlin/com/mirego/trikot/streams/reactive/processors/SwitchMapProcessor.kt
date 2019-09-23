package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.foundation.concurrent.AtomicReference
import com.mirego.trikot.streams.cancellable.CancellableManagerProvider
import com.mirego.trikot.streams.reactive.PublisherDescribable
import com.mirego.trikot.streams.reactive.subscribe
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

typealias SwitchMapProcessorBlock<T, R> = (T) -> Publisher<R>

class SwitchMapProcessor<T, R>(parentPublisher: Publisher<T>, private var block: SwitchMapProcessorBlock<T, R>, name: String? = null) :
    AbstractProcessor<T, R>(parentPublisher, name) {

    override fun createSubscription(subscriber: Subscriber<in R>): ProcessorSubscription<T, R> {
        return SwitchMapProcessorSubscription(subscriber, block, this)
    }

    class SwitchMapProcessorSubscription<T, R>(
        private val subscriber: Subscriber<in R>,
        private val block: SwitchMapProcessorBlock<T, R>,
        publisherDescribable: PublisherDescribable
    ) : ProcessorSubscription<T, R>(subscriber, publisherDescribable) {
        private val cancellableManagerProvider = CancellableManagerProvider()
        private val isCompleted = AtomicReference<Boolean>(false)
        private val isChildCompleted = AtomicReference<Boolean>(false)
        private val currentPublisher = AtomicReference<Publisher<R>?>(null)
        private val onNextValidation = AtomicReference(0)

        override fun onCancel(s: Subscription) {
            super.onCancel(s)
            cancellableManagerProvider.cancel()
        }

        override fun onComplete() {
            isCompleted.setOrThrow(false, true)
            dispatchCompletedIfNeeded()
        }

        override fun onNext(t: T, subscriber: Subscriber<in R>) {
            onNextValidation.setOrThrow(0, 1)
            isChildCompleted.setOrThrow(isChildCompleted.value, false)

            val newPublisher = block(t)
            currentPublisher.setOrThrow(currentPublisher.value, newPublisher)
            newPublisher.subscribe(cancellableManagerProvider.cancelPreviousAndCreate(),
                onNext = { subscriber.onNext(it) },
                onError = { subscriber.onError(it) },
                onCompleted = {
                    isChildCompleted.setOrThrow(isChildCompleted.value, true)
                    dispatchCompletedIfNeeded()
                })

            onNextValidation.setOrThrow(1, 0)
        }

        private fun dispatchCompletedIfNeeded() {
            if (isChildCompleted.value && isCompleted.value) {
                subscriber.onComplete()
            }
        }
    }
}
