package com.cubber.tiime.utils

import java.text.Normalizer

/**
 * Created by mike on 28/09/17.
 */

object Filters {

    private val diacritics = "[\\p{InCombiningDiacriticalMarks}]".toRegex()

    fun clean(input: CharSequence?): String? {
        return if (input != null) {
            Normalizer.normalize(input, Normalizer.Form.NFD)
                    .replace(diacritics, "")
                    .toLowerCase()
        } else {
            null
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