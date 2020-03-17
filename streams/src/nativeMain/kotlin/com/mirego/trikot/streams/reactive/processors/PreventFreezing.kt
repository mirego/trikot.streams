package com.mirego.trikot.streams.reactive.processors

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.native.concurrent.ThreadLocal
import kotlin.native.concurrent.ensureNeverFrozen
import kotlin.native.concurrent.isFrozen

class OnMainTreadPublisher<T>(
    private val parentPublisher: Publisher<T>,
    private val queues: iOSDispatchQueues
) : Publisher<T> {
    @ThreadLocal
    var increment = 0
    val associated = HashMap<Int, Subscriber<T>>()

    init {
        if (!parentPublisher.isFrozen) {
            throw IllegalStateException("ParentPublisher must be frozen")
        }
        ensureNeverFrozen()
    }

    override fun subscribe(s: Subscriber<in T>) {
        if (!queues.isMainThread) {
            throw IllegalStateException("Must subscribe on main thread")
        }
        val generatedId = increment
        increment++

        queues.onBackgroundThread {
            parentPublisher.subscribe(object : Subscriber<T> {
                override fun onSubscribe(s: Subscription) {
                    queues.onMainThread {
                        associated[generatedId]!!.onSubscribe(object : Subscription {
                            override fun request(n: Long) {
                                s.request(n)
                            }

                            override fun cancel() {
                                queues.onMainThread {
                                    s.cancel()
                                    associated.remove(generatedId)
                                }
                            }
                        })
                    }
                }

                override fun onNext(t: T) {
                    queues.onMainThread {
                        associated[generatedId]!!.onNext(t)
                    }
                }

                override fun onError(t: Throwable) {
                    queues.onMainThread {
                        associated[generatedId]!!.onError(t)
                    }
                }

                override fun onComplete() {
                    queues.onMainThread {
                        associated[generatedId]!!.onComplete()
                    }
                }
            })
        }
    }
}

interface iOSDispatchQueues {
    val isMainThread: Boolean
    fun onMainThread(block: () -> Unit)
    fun onBackgroundThread(block: () -> Unit)
}
