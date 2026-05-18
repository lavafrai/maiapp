package ru.lavafrai.maiapp.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class StringSerializer<T> : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("String", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T {
        return deserialize(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(serialize(value))
    }

    abstract fun serialize(data: T): String
    abstract fun deserialize(data: String): T
}