package org.hammerlab.genomics.loci.map

import hammerlab.test.Suite
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.Interval
import org.hammerlab.genomics.reference.test.LociConversions.intToLocus
import org.hammerlab.genomics.reference.test.{ ClearContigNames, ContigNameConversions }

class LociMapSuite
  extends Suite
    with ContigNameConversions
    with ClearContigNames
    with LociSetUtil
    with cmps {

  test("properties of empty LociMap") {
    val emptyMap = LociMap[String]()

    ==(emptyMap.count, 0)
    ==(emptyMap.toString, "")
    ==(emptyMap, LociMap[String]())
  }

  test("basic map operations") {
    val lociMap = LociMap(
      ("chr1",  100, 200, "A"),
      ("chr20", 200, 201, "B")
    )

    ==(lociMap.count, 101)
    ==(lociMap.toString, "chr1:100-200=A,chr20:200-201=B")
    ==(lociMap.contigs.map(_.name), Seq("chr1", "chr20"))

    lociMap should not equal LociMap[String]()

    ==(
      lociMap.inverse,
      Map(
        "A" → lociSet("chr1:100-200"),
        "B" → lociSet("chr20:200-201")
      )
    )

    ==(lociMap("chr1").toString, "chr1:100-200=A")
    ==(lociMap("chr20").toString, "chr20:200-201=B")
  }

  test("asInverseMap with repeated values") {
    val lociMap = LociMap(
      ("chr1", 100, 200, "A"),
      ("chr2", 200, 300, "A"),
      ("chr3", 400, 500, "B")
    )

    // asInverseMap stuffs all Loci with the same value into a LociSet.
    ==(
      lociMap.inverse,
      Map(
        "A" -> lociSet("chr1:100-200,chr2:200-300"),
        "B" -> lociSet("chr3:400-500")
      )
    )

    ==(lociMap.count, 300)
    ==(lociMap.toString, "chr1:100-200=A,chr2:200-300=A,chr3:400-500=B")
  }

  test("range coalescing") {
    val lociMap = LociMap(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B"),
      ("chr1", 150, 160, "C"),
      ("chr1", 180, 240, "A")
    )

    lociMap.inverse ===
      Map(
        "A" -> lociSet("chr1:100-150,chr1:160-240"),
        "B" -> lociSet("chr1:400-500"),
        "C" -> lociSet("chr1:150-160")
      )

    ==(lociMap.count, 240)
    ==(lociMap.toString, "chr1:100-150=A,chr1:150-160=C,chr1:160-240=A,chr1:400-500=B")
  }

  test("spanning equal values merges") {
    val map = LociMap(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B"),
      ("chr1", 300, 400, "A"),
      ("chr1", 199, 301, "A")
    )

    map.inverse ===
      Map(
        "A" -> lociSet("chr1:100-400"),
        "B" -> lociSet("chr1:400-500")
      )

    map("chr1").asMap ===
      Map(
        Interval(100, 400) -> "A",
        Interval(400, 500) -> "B"
      )

    ==(map.count, 400)
  }

  test("bridging equal values merges") {
    val map = LociMap(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B"),
      ("chr1", 300, 400, "A"),
      ("chr1", 200, 300, "A")
    )

    map.inverse ===
      Map(
        "A" -> lociSet("chr1:100-400"),
        "B" -> lociSet("chr1:400-500")
      )

    map("chr1").asMap ===
      Map(
        Interval(100, 400) -> "A",
        Interval(400, 500) -> "B"
      )

    ==(map.count, 400)
  }
}
