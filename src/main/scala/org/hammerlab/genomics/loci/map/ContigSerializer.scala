package org.hammerlab.genomics.loci.map

import com.esotericsoftware.kryo.io.{ Input, Output }
import com.esotericsoftware.kryo.{ Kryo, Serializer ⇒ KryoSerializer }
import org.hammerlab.genomics.reference.{ ContigName, Locus }

/**
 * We serialize a Contig as its name, the number of ranges, and the ranges themselves (two longs and a value each).
 */
class ContigSerializer[T] extends KryoSerializer[Contig[T]] {
  def write(kryo: Kryo, output: Output, obj: Contig[T]) = {
    kryo.writeObject(output, obj.name)
    output.writeLong(obj.asMap.size)
    obj.asMap.foreach {
      case (range, value) =>
        output.writeLong(range.start.locus)
        output.writeLong(range.end.locus)
        kryo.writeClassAndObject(output, value)
    }
  }

  def read(kryo: Kryo, input: Input, klass: Class[Contig[T]]): Contig[T] = {
    val builder = LociMap.newBuilder[T]
    val contig = kryo.readObject(input, classOf[ContigName])
    val count = input.readLong()
    (0L until count).foreach { _ =>
      val start = input.readLong()
      val end = input.readLong()
      val value: T = kryo.readClassAndObject(input).asInstanceOf[T]
      builder.put(contig, Locus(start), Locus(end), value)
    }
    builder.result(contig)
  }
}

