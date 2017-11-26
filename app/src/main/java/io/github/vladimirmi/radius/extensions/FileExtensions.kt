package io.github.vladimirmi.radius.extensions

import java.io.File
import java.io.PrintWriter

/**
 * Created by Vladimir Mikhalev 10.11.2017.
 */

fun File.clear() {
    PrintWriter(this).close()
}