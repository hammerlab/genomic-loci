package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.iterator.LociIterator
import org.hammerlab.genomics.reference.test.TestInterval
import org.hammerlab.test.Suite
import org.hammerlab.genomics.reference.test.LocusUtil._

class LociIteratorSuite extends Suite {

  def loci(intervals: (Int, Int)*): LociIterator =
    new LociIterator(
      (for {
        (start, end) <- intervals
      } yield
        TestInterval(start, end)
      ).iterator.buffered
    )

  test("simple") {
    loci(100 -> 110).toList === (100 until 110)
  }

  test("skipTo") {
    val it = loci(100 -> 110)
    it.skipTo(103)
    it.head === 103
    it.toList === (103 until 110)
  }

  test("intervals") {
    loci(100 -> 110, 120 -> 130).toList === ((100 until 110) ++ (120 until 130))
  }
}
