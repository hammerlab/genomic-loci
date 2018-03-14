package org.hammerlab.genomics.loci.iterator

import hammerlab.test.Suite
import org.hammerlab.genomics.reference.test.IntervalsUtil
import org.hammerlab.genomics.reference.test.LociConversions._
import org.hammerlab.genomics.reference.{ Interval, Locus }

class SkippableLociIteratorSuite
  extends Suite
    with IntervalsUtil {

  def strs =
    TestSkippableLociIterator(
      10 → "a",
      11 → "b",
      20 → "c",
      21 → "d",
      30 → "e",
      31 → "f",
      33 → "g",
      34 → "h",
      40 → "i",
      50 → "j"
    )

  test("no skips") {
    strs.toSeq should ===(
      List(
        10 → "a",
        11 → "b",
        20 → "c",
        21 → "d",
        30 → "e",
        31 → "f",
        33 → "g",
        34 → "h",
        40 → "i",
        50 → "j"
      )
    )
  }

  test("skip all") {
    val it = strs
    it.skipTo(51)
    it.toList should be(Nil)
  }

  test("misc skips") {
    val it = strs
    it.skipTo(15)
    it.next() should ===(20 → "c")
    it.skipTo(30)
    it.next() should ===(30 → "e")
    intercept[IllegalArgumentException] {
      it.skipTo(30)
    }
    it.next() should ===(31 → "f")
    it.skipTo(32)
    it.next() should ===(33 → "g")
    it.skipTo(34)
    it.next() should ===(34 → "h")
    it.skipTo(41)
    it.next() should ===(50 → "j")
    it.hasNext should ===(false)
  }

  test("intersect") {
    strs.intersect(
      new LociIterator(
        Iterator(
          Interval( 8, 11),
          Interval(14, 16),
          Interval(30, 35),
          Interval(38, 42),
          Interval(50, 51)
        ).buffered
      )
    ).toSeq ===
      Seq(
        10 → "a",
        30 → "e",
        31 → "f",
        33 → "g",
        34 → "h",
        40 → "i",
        50 → "j"
      )
  }
}

case class TestSkippableLociIterator(elems: (Int, String)*)
  extends SkippableLocusKeyedIterator[String] {

  val it = elems.iterator.map(t => (t._1: Locus) → t._2).buffered

  override def _advance: Option[(Locus, String)] = {
    if (!it.hasNext)
      None
    else {
      val (nextLocus, str) = it.next
      if (nextLocus < locus)
        _advance
      else {
        locus = nextLocus
        Some(nextLocus → str)
      }
    }
  }

  override def skipTo(newLocus: Locus): TestSkippableLociIterator.this.type = {
    super.skipTo(newLocus)
    while (it.hasNext && locusFn(it.head) < locus) it.next()
    this
  }
}
