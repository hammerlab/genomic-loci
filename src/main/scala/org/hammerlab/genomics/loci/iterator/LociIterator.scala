package org.hammerlab.genomics.loci.iterator

import org.hammerlab.genomics.reference.{Interval, Locus}

class LociIterator(intervals: BufferedIterator[Interval]) extends SkippableLociIterator[Locus] {

  override def locusFn: (Locus) => Locus = x => x

  override def _advance: Option[Locus] = {
    if (!intervals.hasNext)
      None
    else if (intervals.head.contains(locus))
      Some(locus)
    else if (intervals.head.end <= locus) {
      intervals.next()
      _advance
    } else {
      locus = intervals.head.start
      Some(locus)
    }
  }
}
