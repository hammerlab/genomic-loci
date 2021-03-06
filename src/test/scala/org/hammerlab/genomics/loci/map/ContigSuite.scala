package org.hammerlab.genomics.loci.map

import com.google.common.collect.{ ImmutableRangeMap, Range }
import hammerlab.test.Suite
import org.hammerlab.genomics.reference.Locus
import org.hammerlab.genomics.reference.test.LociConversions._
import org.hammerlab.genomics.reference.test.{ ClearContigNames, ContigNameConversions }

class ContigSuite
  extends Suite
    with ContigNameConversions
    with ClearContigNames {

  test("empty") {
    val contigMap = new Contig("chr1", ImmutableRangeMap.builder[Locus, String]().build())

    ==(contigMap.get(100), None)
    contigMap.getAll(0, 10000) should be(Set())
    ==(contigMap.count, 0)
    ==(contigMap.toString, "")
  }

  test("basic operations") {
    type JLong = java.lang.Long
    val range100to200 = ImmutableRangeMap.of[Locus, String](Range.closedOpen[Locus](100, 200), "A")
    val range200to300 = ImmutableRangeMap.of[Locus, String](Range.closedOpen[Locus](200, 300), "B")

    val contigMap =
      new Contig(
        "chr1",
        ImmutableRangeMap
          .builder[Locus, String]()
          .putAll(range100to200)
          .putAll(range200to300)
          .build()
      )

    contigMap.get(99) should be(None)
    ==(contigMap.get(100), Some("A"))
    ==(contigMap.get(199), Some("A"))
    ==(contigMap.get(200), Some("B"))
    ==(contigMap.get(299), Some("B"))
    contigMap.get(300) should be(None)

    contigMap.getAll(0, 100) should be(Set())
    ==(contigMap.getAll(0, 101), Set("A"))
    ==(contigMap.getAll(199, 200), Set("A"))
    ==(contigMap.getAll(199, 201), Set("A", "B"))
    ==(contigMap.getAll(200, 201), Set("B"))
    ==(contigMap.getAll(0, 10000), Set("A", "B"))

    ==(contigMap.count, 200)
    ==(contigMap.toString, "chr1:100-200=A,chr1:200-300=B")
  }

  test("getAll") {
    val lociMap =
      LociMap(
        ("chrM",    0,  8286, 0),
        ("chrM", 8286, 16571, 1)
      )

    ==(lociMap("chrM").getAll(5, 10), Set(0))
    ==(lociMap("chrM").getAll(10000, 11000), Set(1))
  }
}
