package com.mirego.trikot.streams.reactive

object Publishers {
    fun <T> behaviorSubject(value: T? = null, name: String = "Publishers.behaviorSubject"): BehaviorSubject<T> {
        return BehaviorSubjectImpl(value, name)
    }

    fun <T> publishSubject(name: String = "Publishers.publishSubject"): PublishSubject<T> {
        return PublishSubjectImpl(name)
    }
}
