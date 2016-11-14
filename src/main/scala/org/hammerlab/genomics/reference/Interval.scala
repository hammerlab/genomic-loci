package org.hammerlab.genomics.reference

import scala.collection.Iterator

trait Interval extends Ordered[Interval] {
  /** Start position on the genome, inclusive. Must be non-negative. */
  def start: Locus

  /** The end position on the genome, *exclusive*. Must be non-negative. */
  def end: Locus

  def contains(locus: Locus): Boolean = start <= locus && locus < end

  /** Iterate through elements in the range. */
  def iterator(): Iterator[Long] = new Iterator[Locus] {
    private var i = start
    override def hasNext: Boolean = i < end
    override def next(): Long =
      if (hasNext) { val result = i; i += 1; result }
      else Iterator.empty.next()
  }
  /** Number of elements in the range. */
  def length: NumLoci = end - start

  /** Comparisons between ranges. Order is DESCENDING (i.e. reversed) from by start. */
  override def compare(other: Interval): Int = {
    val diff = start - other.start
    if (diff < 0) -1
    else if (diff == 0) 0
    else 1
  }
}

object Interval {
  def apply(start: Locus, end: Locus): Interval = IntervalImpl(start, end)

  def apply(t: (Locus, Locus)): Interval = IntervalImpl(t._1, t._2)

  def unapply(i: Interval): Option[(Locus, Locus)] = Some((i.start, i.end))

  def orderByEndDesc[I <: Interval] =
    new Ordering[I] {
      override def compare(x: I, y: I): Int = y.end.compare(x.end)
    }
}

private case class IntervalImpl(start: Locus, end: Locus) extends Interval
