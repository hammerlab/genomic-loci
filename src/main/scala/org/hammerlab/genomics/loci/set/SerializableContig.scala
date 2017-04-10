package org.hammerlab.genomics.loci.set

import java.io.{ ObjectInputStream, ObjectOutputStream }

import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus }

private class SerializableContig
  extends Serializable {
  var contigName: ContigName = _
  var numRanges: Int = 0
  var ranges: Iterator[Interval] = _
  implicit var factory: Factory = _

  private def writeObject(out: ObjectOutputStream): Unit = {
    out.writeObject(factory)
    out.writeUTF(contigName.name)
    out.writeInt(numRanges)
    for {
      Interval(start, end) ← ranges
    } {
      out.writeLong(start.locus)
      out.writeLong(end.locus)
    }
  }

  private def readObject(in: ObjectInputStream): Unit = {
    factory = in.readObject().asInstanceOf[Factory]
    contigName = in.readUTF()
    numRanges = in.readInt()
    ranges =
      (0 until numRanges)
        .map { _ ⇒
          val start = Locus(in.readLong())
          val end = Locus(in.readLong())
          Interval(start, end)
        }
        .iterator
  }

  protected def readResolve: Object =
    Contig(contigName, ranges)
}

private object SerializableContig {
  def apply(contig: Contig)(implicit factory: Factory): SerializableContig = {
    val sc = new SerializableContig
    sc.contigName = contig.name
    sc.numRanges = contig.numRanges
    sc.ranges = contig.ranges
    sc.factory = factory
    sc
  }
}
