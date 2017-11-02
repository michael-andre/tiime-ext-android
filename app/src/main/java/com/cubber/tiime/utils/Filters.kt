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

inline fun <T> Iterable<T>.filterCleaned(query: CharSequence, mapper: (T) -> Array<String?>) : List<T> {
    val cleanQuery = Filters.clean(query)
    if (cleanQuery?.isEmpty() != false) return emptyList()
    return this.filter {
        mapper(it)
                .map { Filters.clean(it) }
                .any { s -> s?.contains(cleanQuery) ?: false }
    }
}