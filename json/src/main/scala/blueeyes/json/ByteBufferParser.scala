package blueeyes.json

import scala.annotation.{switch, tailrec}
import java.nio.ByteBuffer

/**
 * Basic ByteBuffer parser.
 */
private[json] final class ByteBufferParser(src: ByteBuffer)
extends SyncParser with ByteBasedParser {
  final val start = src.position
  final val limit = src.limit - start

  var line = 0
  protected[this] final def newline(i: Int) { line += 1 }
  protected[this] final def column(i: Int) = i

  final def close() { src.position(src.limit) }
  final def reset(i: Int): Int = i
  final def checkpoint(state: Int, i: Int, stack: List[Context]) {}
  final def byte(i: Int): Byte = src.get(i + start)
  final def at(i: Int): Char = src.get(i + start).toChar

  final def at(i: Int, k: Int): String = {
    val len = k - i
    val arr = new Array[Byte](len)
    src.position(i + start)
    src.get(arr, 0, len)
    src.position(start)
    new String(arr, utf8)
  }

  final def atEof(i: Int) = i >= limit
}
