package pereira.agnaldo.audiorecorder

import java.io.File
import kotlin.random.Random

class Helper {

    companion object {
        fun map(x: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float): Float {
            return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
        }

        fun getRandomBytesSample(byteSampleCount: Int): ByteArray {
            val bytes = ArrayList<Byte>()
            for (i in 0 until byteSampleCount) {
                val byte = if (i % 2 == 0) {
                    Random.nextInt(0, Byte.MAX_VALUE.toInt())
                } else {
                    Random.nextInt(Byte.MIN_VALUE.toInt(), 0)
                }
                bytes.add(byte.toByte())
            }
            return bytes.toByteArray()
        }

        fun getBytesSampleWithZeros(byteSampleCount: Int): ByteArray =
            ByteArray(byteSampleCount)

    }

}

fun File.deleteIfExists() = takeIf { this.exists() }?.delete()
