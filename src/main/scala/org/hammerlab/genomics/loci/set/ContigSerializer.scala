package org.hammerlab.genomics.loci.set

import com.esotericsoftware.kryo.io.{ Input, Output }
import com.esotericsoftware.kryo.{ Kryo, Serializer ⇒ KryoSerializer }
import com.google.common.collect.{ TreeRangeSet, Range ⇒ JRange }
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus }

// We serialize a LociSet simply by writing out its constituent Contigs.
class ContigSerializer extends KryoSerializer[Contig] {

  def write(kryo: Kryo, output: Output, obj: Contig) = {
    kryo.writeObject(output, obj.name)
    output.writeInt(obj.ranges.length)
    for {
      Interval(start, end) <- obj.ranges
    } {
      output.writeLong(start.locus)
      output.writeLong(end.locus)
    }
  }

  def read(kryo: Kryo, input: Input, klass: Class[Contig]): Contig = {
    val name = kryo.readObject(input, classOf[ContigName])
    val length = input.readInt()
    val treeRangeSet = TreeRangeSet.create[Locus]()
    val ranges = (0 until length).foreach { _ =>
      treeRangeSet.add(JRange.closedOpen[Locus](input.readLong(), input.readLong()))
    }
    Contig(name, treeRangeSet)
  }
}
