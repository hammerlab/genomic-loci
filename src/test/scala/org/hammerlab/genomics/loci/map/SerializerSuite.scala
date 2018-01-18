package org.hammerlab.genomics.loci.map

import org.hammerlab.genomics.reference.test.ClearContigNames
import org.hammerlab.genomics.reference.test.LociConversions.intToLocus
import org.hammerlab.genomics.reference.{ ContigName, Locus }
import org.hammerlab.spark.test.suite.{ KryoSparkSuite, SparkSerialization }

class SerializerSuite
  extends KryoSparkSuite
    with SparkSerialization
    with ClearContigNames {

  register(
    classOf[LociMap[Nothing]]
  )

  def check(
    ranges: (ContigName, Locus, Locus, String)*
  )(
    expectedBytes: Int,
    numRanges: Int,
    count: Int
  ) = {
    val beforeMap = LociMap(ranges: _*)

    beforeMap("chr1").asMap.size should ===(numRanges)
    beforeMap("chr1").count should ===(count)

    val bytes = serialize(beforeMap)
    bytes.array.length should ===(expectedBytes)

    val afterMap: LociMap[String] = deserialize[LociMap[String]](bytes)

    beforeMap should ===(afterMap)
  }

  test("empty") { check()(9, 0, 0) }

  test("1") {
    check(
      ("chr1", 100, 200, "A")
    )(
      40, 1, 100
    )
  }

  test("2") {
    check(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B")
    )(
      59, 2, 200
    )
  }

  test("3") {
    check(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B"),
      ("chr1", 600, 700, "C")
    )(
      78, 3, 300
    )
  }

  test("4") {
    check(
      ("chr1", 100, 200, "A"),
      ("chr1", 400, 500, "B"),
      ("chr1", 600, 700, "C"),
      ("chr1", 700, 800, "A")
    )(
      97, 4, 400
    )
  }
}
