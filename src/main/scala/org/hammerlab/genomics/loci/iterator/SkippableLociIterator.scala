package org.hammerlab.genomics.loci.iterator

import org.hammerlab.genomics.reference.Locus
import hammerlab.iterator._

abstract class SkippableLociIterator[T]
  extends SimpleIterator[T] {

  def locusFn: T => Locus

  var locus = Locus(0)

  override def postNext(): Unit = {
    locus = locus.next
  }

  def skipTo(newLocus: Locus): this.type = {
    if (newLocus > locus) {
      locus = newLocus
      clear()
    } else if (newLocus < locus) {
      throw new IllegalArgumentException(s"Attempting to rewind iterator from $locus to $newLocus")
    }
    this
  }

  def intersect(loci: LociIterator): SkippableLociIterator[T] = {
    val self = this
    new SkippableLociIterator[T] {
      override def locusFn: (T) => Locus = self.locusFn

      override def _advance: Option[T] = {
        if (!loci.hasNext) return None
        if (!self.hasNext) return None

        val nextAllowedLocus = loci.head
        val obj = self.head
        val nextObjectLocus = locusFn(obj)

        if (nextObjectLocus > nextAllowedLocus) {
          loci.skipTo(nextObjectLocus)
          _advance
        } else if (nextAllowedLocus > nextObjectLocus) {
          self.skipTo(nextAllowedLocus)
          _advance
        } else {
          loci.next()
          self.next()
          Some(obj)
        }
      }

      override def skipTo(newLocus: Locus): this.type = {
        super.skipTo(newLocus)
        loci.skipTo(locus)
        self.skipTo(locus)
        this
      }
    }
  }
}

abstract class SkippableLocusKeyedIterator[T] extends SkippableLociIterator[(Locus, T)] {
  override def locusFn: ((Locus, T)) => Locus = _._1
}
