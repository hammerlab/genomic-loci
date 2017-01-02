package org.hammerlab.genomics.loci.map

import com.google.common.collect.{ ImmutableRangeMap, Range }
import org.hammerlab.genomics.reference.Locus
import org.hammerlab.test.Suite
import org.hammerlab.genomics.reference.test.LocusUtil._

class ContigSuite extends Suite {
  test("empty") {
    val contigMap = new Contig("chr1", ImmutableRangeMap.builder[Locus, String]().build())

    contigMap.get(100) should ===(None)
    contigMap.getAll(0, 10000) should be(Set())
    contigMap.count should ===(0)
    contigMap.toString should ===("")
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
    contigMap.get(100) should ===(Some("A"))
    contigMap.get(199) should ===(Some("A"))
    contigMap.get(200) should ===(Some("B"))
    contigMap.get(299) should ===(Some("B"))
    contigMap.get(300) should be(None)

    contigMap.getAll(0, 100) should be(Set())
    contigMap.getAll(0, 101) should ===(Set("A"))
    contigMap.getAll(199, 200) should ===(Set("A"))
    contigMap.getAll(199, 201) should ===(Set("A", "B"))
    contigMap.getAll(200, 201) should ===(Set("B"))
    contigMap.getAll(0, 10000) should ===(Set("A", "B"))

    contigMap.count should ===(200)
    contigMap.toString should ===("chr1:100-200=A,chr1:200-300=B")
  }

  test("getAll") {
    val lociMap =
      LociMap(
        ("chrM",    0,  8286, 0),
        ("chrM", 8286, 16571, 1)
      )

    lociMap("chrM").getAll(5, 10) should ===(Set(0))
    lociMap("chrM").getAll(10000, 11000) should ===(Set(1))
  }
}
