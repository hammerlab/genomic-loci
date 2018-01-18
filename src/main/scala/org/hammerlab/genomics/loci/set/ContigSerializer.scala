package org.hammerlab.genomics.loci.set

import com.esotericsoftware.kryo.io.{ Input, Output }
import com.esotericsoftware.kryo.{ Kryo, Serializer ⇒ KryoSerializer }
import com.google.common.collect.{ TreeRangeSet, Range ⇒ JRange }
import JRange.closedOpen
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus }

/** Serialize a [[LociSet]] simply by writing out its [[Contig]]s. */
class ContigSerializer extends KryoSerializer[Contig] {

  def write(kryo: Kryo, output: Output, obj: Contig) = {
    kryo.writeObject(output, obj.name)
    output.writeInt(obj.numRanges)
    for {
      Interval(start, end) ← obj.ranges
    } {
      output.writeLong(start.locus)
      output.writeLong(end.locus)
    }
  }

  def read(kryo: Kryo, input: Input, klass: Class[Contig]): Contig = {
    val name = kryo.readObject(input, classOf[ContigName])
    val numRanges = input.readInt()
    val treeRangeSet = TreeRangeSet.create[Locus]()
    val ranges =
      (0 until numRanges).foreach { _ ⇒
        treeRangeSet.add(
          closedOpen(
            Locus(input.readLong()),
            Locus(input.readLong())
          )
        )
      }

    Contig(name, treeRangeSet)
  }
}
