package com.mirego.trikot.streams.reactive.processors

import com.mirego.trikot.streams.reactive.Publishers
import com.mirego.trikot.streams.reactive.filter
import com.mirego.trikot.streams.reactive.map
import org.reactivestreams.Publisher

data class NTuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class NTuple5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

fun <T> combine(publishers: List<Publisher<T>>): Publisher<List<T?>> {
    return if (publishers.count() == 0) {
        Publishers.behaviorSubject(emptyList())
    } else {
        val publisher = publishers.first()
        val otherPublishers = if (publishers.count() > 1) publishers.subList(
            1, publishers.count()) else emptyList()

        publisher.combine(otherPublishers)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T, R> Publisher<T>.combine(publisher: Publisher<R>): Publisher<Pair<T?, R?>> {
    return (this as Publisher<Any>).combine(listOf(publisher) as List<Publisher<Any>>)
        .map { list ->
            list[0] as? T to list[1] as? R
        }
}

@Suppress("UNCHECKED_CAST")
fun <T, R> Publisher<T>.safeCombine(publisher: Publisher<R>): Publisher<Pair<T, R>> {
    return (this as Publisher<Any>).combine(listOf(publisher) as List<Publisher<Any>>)
        .filter { list -> list[0] as? T != null && list[1] as? T != null }
        .map { list ->
            list[0] as T to list[1] as R
        }
}

@Suppress("UNCHECKED_CAST")
fun <T, R1, R2> Publisher<T>.safeCombine(publisher1: Publisher<R1>, publisher2: Publisher<R2>): Publisher<Triple<T, R1, R2>> {
    return (this as Publisher<Any>).combine(listOf(publisher1, publisher2) as List<Publisher<Any>>)
        .filter { list -> list[0] as? T != null && list[1] as? T != null && list[2] as? T != null }
        .map { list ->
            Triple(list[0] as T, list[1] as R1, list[2] as R2)
        }
}

@Suppress("UNCHECKED_CAST")
fun <T, R1, R2, R3> Publisher<T>.safeCombine(
    publisher1: Publisher<R1>,
    publisher2: Publisher<R2>,
    publisher3: Publisher<R3>
): Publisher<NTuple4<T, R1, R2, R3>> {
    return (this as Publisher<Any>).combine(listOf(publisher1, publisher2, publisher3) as List<Publisher<Any>>)
        .filter { list -> list[0] as? T != null && list[1] as? R1 != null && list[2] as? R2 != null && list[3] as? R3 != null }
        .map { list ->
            NTuple4(list[0] as T, list[1] as R1, list[2] as R2, list[3] as R3)
        }
}

@Suppress("UNCHECKED_CAST")
fun <T, R1, R2, R3, R4> Publisher<T>.safeCombine(
    publisher1: Publisher<R1>,
    publisher2: Publisher<R2>,
    publisher3: Publisher<R3>,
    publisher4: Publisher<R4>
): Publisher<NTuple5<T, R1, R2, R3, R4>> {
    return (this as Publisher<Any>).combine(listOf(publisher1, publisher2, publisher3, publisher4) as List<Publisher<Any>>)
        .filter { list -> list[0] as? T != null && list[1] as? R1 != null && list[2] as? R2 != null && list[3] as? R3 != null && list[4] as? R4 != null }
        .map { list ->
            NTuple5(list[0] as T, list[1] as R1, list[2] as R2, list[3] as R3, list[4] as R4)
        }
}

fun <T> Publisher<T>.combine(publishers: List<Publisher<T>>): Publisher<List<T?>> {
    return CombineLatestProcessor(this, publishers)
}
