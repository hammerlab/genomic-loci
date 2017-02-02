package org.hammerlab.genomics.loci.map

import org.hammerlab.genomics.reference.test.LociConversions.intToLocus
import org.hammerlab.genomics.reference.{ ContigName, Locus }
import org.hammerlab.spark.test.suite.{ KryoSparkSuite, SparkSerialization }

class SerializerSuite
  extends KryoSparkSuite(classOf[Registrar])
  with SparkSerialization {

  def testSerde(
    name: String
  )(
    ranges: (ContigName, Locus, Locus, String)*
  )(
    expectedBytes: Int,
    numRanges: Int,
    count: Int
  ) = {
    test(name) {
      val beforeMap = LociMap(ranges: _*)

      beforeMap("chr1").asMap.size should ===(numRanges)
      beforeMap("chr1").count should ===(count)

      val bytes = serialize(beforeMap)
      bytes.array.length should ===(expectedBytes)

      val afterMap: LociMap[String] = deserialize[LociMap[String]](bytes)

      beforeMap should ===(afterMap)
    }
  }

  testSerde("empty")()(9, 0, 0)

  testSerde("1")(
    ("chr1", 100L, 200L, "A")
  )(
    40, 1, 100
  )

  testSerde("2")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B")
  )(
    59, 2, 200
  )

  testSerde("3")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B"),
    ("chr1", 600L, 700L, "C")
  )(
    78, 3, 300
  )

  testSerde("4")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B"),
    ("chr1", 600L, 700L, "C"),
    ("chr1", 700L, 800L, "A")
  )(
    97, 4, 400
  )
}
