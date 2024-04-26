package python

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.NumberFormatException
import java.math.BigInteger

object BigIntegerSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): BigInteger = BigInteger.valueOf(decoder.decodeLong()) // todo possible overflows

    override fun serialize(
        encoder: Encoder,
        value: BigInteger,
    ) = encoder.encodeString(value.toString())
}

fun String.toBigIntegerOrNull(base: Int): BigInteger? {
    return try {
        BigInteger(this, base)
    } catch (ex: NumberFormatException) {
        null
    }
}