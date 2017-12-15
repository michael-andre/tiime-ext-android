package com.cubber.tiime.utils

import java.text.Normalizer

/**
 * Created by mike on 28/09/17.
 */

object Filters {

    fun clean(input: CharSequence?): String? {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")
                .toLowerCase()
    }

    fun <T> filter(collection: Collection<T>, query: String, mapper: (T) -> Iterable<String?>): Collection<T> {
        val cleanQuery = clean(query)
        if (cleanQuery?.isEmpty() != false) return emptyList()
        return collection.filter {
            mapper(it)
                    .map { clean(it) }
                    .any { s -> s?.contains(cleanQuery) ?: false }
        }
    }

}

inline fun <T> Sequence<T>.filterCleaned(query: CharSequence, crossinline mapper: (T) -> Sequence<String?>) : Sequence<T> {
    val cleanQuery = Filters.clean(query)
    if (cleanQuery?.isEmpty() != false) return emptySequence()
    return this.filter {
        mapper(it).any { s -> Filters.clean(s)?.contains(cleanQuery) ?: false }
    }
}

inline fun <T> Iterable<T>.filterCleaned(query: CharSequence, crossinline mapper: (T) -> Sequence<String?>) : Sequence<T> {
    return asSequence().filterCleaned(query, mapper)
}