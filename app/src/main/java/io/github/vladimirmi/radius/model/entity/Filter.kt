package io.github.vladimirmi.radius.model.entity

/**
 * Created by Vladimir Mikhalev 08.01.2018.
 */

enum class Filter(val predicate: (Station) -> Boolean) {

    FAVORITE(Station::favorite),
    DEFAULT({ true })
}