package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.iterator.LociIterator
import org.hammerlab.genomics.reference.test.TestInterval
import org.hammerlab.test.Suite

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
    loci(100 -> 110).toList should be(100 until 110)
  }

  test("skipTo") {
    val it = loci(100 -> 110)
    it.skipTo(103)
    it.head should be(103)
    it.toList should be(103 until 110)
  }

  test("intervals") {
    loci(100 -> 110, 120 -> 130).toList should be((100 until 110) ++ (120 until 130))
  }
}
