package com.mirego.trikot.streams.reactive

interface PublisherDescribable {
    val name: String
    fun describeProperties(): Map<String, Any?>
}

fun PublisherDescribable.describe(indent: Int): String {
    val padding = " ".repeat(indent)
    val propertiesPadding = " ".repeat(indent + 4)
    var properties: String = "${padding}Properties:"
    describeProperties().forEach { (name, value) ->
        (value as? PublisherDescribable)?.let {
            properties += "\n${it.describe(indent + 4)}"
        } ?: run {
            properties += "\n$propertiesPadding$name: $value"
        }
    }

    return """
$padding$name
$properties
"""
}
