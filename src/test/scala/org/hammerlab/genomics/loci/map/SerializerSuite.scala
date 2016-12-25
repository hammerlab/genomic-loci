package org.hammerlab.genomics.loci.map

import org.hammerlab.genomics.reference.{ ContigName, Locus }
import org.hammerlab.spark.test.suite.{ KryoSerializerSuite, SparkSerializerSuite }

class SerializerSuite
  extends KryoSerializerSuite(classOf[Registrar])
  with SparkSerializerSuite {

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

      beforeMap.onContig("chr1").asMap.size === numRanges
      beforeMap.onContig("chr1").count === count

      val bytes = serialize(beforeMap)
      bytes.array.length === expectedBytes

      val afterMap: LociMap[String] = deserialize[LociMap[String]](bytes)

      beforeMap === afterMap
    }
  }

  testSerde("empty")()(10, 0, 0)

  testSerde("1")(
    ("chr1", 100L, 200L, "A")
  )(
    43, 1, 100
  )

  testSerde("2")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B")
  )(
    63, 2, 200
  )

  testSerde("3")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B"),
    ("chr1", 600L, 700L, "C")
  )(
    83, 3, 300
  )

  testSerde("4")(
    ("chr1", 100L, 200L, "A"),
    ("chr1", 400L, 500L, "B"),
    ("chr1", 600L, 700L, "C"),
    ("chr1", 700L, 800L, "A")
  )(
    101, 4, 400
  )
}
