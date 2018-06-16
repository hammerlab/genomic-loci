package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.LociConversions._
import org.hammerlab.genomics.reference.test.{ ClearContigNames, ContigLengthsUtil, ContigNameConversions }
import org.hammerlab.genomics.reference.{ ContigLengths, ContigName, Locus, NumLoci }
import org.hammerlab.kryo.Registration.ClassWithSerializerToRegister
import org.hammerlab.kryo._
import org.hammerlab.spark.test.suite.KryoSparkSuite

import scala.collection.mutable

class LociSetSuite
  extends KryoSparkSuite
    with LociSetUtil
    with ContigNameConversions
    with ClearContigNames
    with ContigLengthsUtil
    with cmps {

  // "loci set invariants" collects some LociSets
  register(
    arr[LociSet],
    classOf[mutable.WrappedArray.ofRef[_]]
  )

  def makeLociSet(str: String, lengths: (ContigName, NumLoci)*): LociSet =
    LociSet(ParsedLoci(str), lengths.toMap)

  test("registration") {
    val reg: ClassWithSerializerToRegister[LociSet] = arr[LociSet]
    reg.serializer should be(Some(LociSet.serializer))
    reg.alsoRegister should be(Some(LociSet.alsoRegister))
  }

  test("properties of empty LociSet") {
    val empty = LociSet()
    empty.contigs should have size 0
    ==(empty.count, 0)
    ==(empty, lociSet(""))
    val empty2 = lociSet("empty1:30-30,empty2:40-40")
    ==(empty, empty2)
  }

  test("count, containment, intersection testing of a loci set") {
    val set = lociSet("chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10")
    ==(set.contigs.map(_.name), Seq("chr20", "chr21"))
    ==(set.count, 135)
    ==(set("chr20").contains(110), true)
    ==(set("chr20").contains(100), true)
    ==(set("chr20").contains(99), false)
    ==(set("chr20").contains(120), false)
    ==(set("chr20").contains(119), true)
    ==(set("chr20").count, 35)
    ==(set("chr20").intersects(0, 5), true)
    ==(set("chr20").intersects(0, 1), true)
    ==(set("chr20").intersects(0, 0), false)
    ==(set("chr20").intersects(7, 8), true)
    ==(set("chr20").intersects(9, 11), true)
    ==(set("chr20").intersects(11, 18), true)
    ==(set("chr20").intersects(18, 19), false)
    ==(set("chr20").intersects(14, 80), true)
    ==(set("chr20").intersects(15, 80), false)
    ==(set("chr20").intersects(120, 130), false)
    ==(set("chr20").intersects(119, 130), true)

    ==(set("chr21").contains(99), false)
    ==(set("chr21").contains(100), true)
    ==(set("chr21").contains(200), false)
    ==(set("chr21").count, 100)
    ==(set("chr21").intersects(110, 120), true)
    ==(set("chr21").intersects(90, 120), true)
    ==(set("chr21").intersects(150, 200), true)
    ==(set("chr21").intersects(150, 210), true)
    ==(set("chr21").intersects(200, 210), false)
    ==(set("chr21").intersects(201, 210), false)
    ==(set("chr21").intersects(90, 100), false)
    ==(set("chr21").intersects(90, 101), true)
    ==(set("chr21").intersects(90, 95), false)
    ==(set("chr21").iterator.toSeq, 100 until 200)
  }

  test("single loci parsing") {
    val set = lociSet("chr1:10000")
    ==(set.count, 1)
    ==(set("chr1").contains( 9999), false)
    ==(set("chr1").contains(10000), true)
    ==(set("chr1").contains(10001), false)
  }

  test("loci set invariants") {
    val sets =
      List(
        "",
        "empty:20-20,empty2:30-30",
        "20:100-200",
        "with_dots.and_underscores..2:100-200",
        "21:300-400",
        "X:5-17,X:19-22,Y:50-60",
        "21:100-200,20:0-10,20:8-15,20:100-120"
      )
      .map(lociSet)

    def checkInvariants(set: LociSet): Unit = {
      set should not be null
      set.toString should not be null
      withClue("invariants for: '%s'".format(set.toString)) {
        ==(lociSet(set.toString), set)
        ==(lociSet(set.toString).toString, set.toString)
        ==(set, set)

        // Test serialization. We hit all sorts of null pointer exceptions here at one point, so we are paranoid about
        // checking every pointer.
        val parallelized = sc.parallelize(List(set))
        val collected = parallelized.collect()
        val result = collected(0)
        ==(result, set)
      }
    }

    sets.foreach(checkInvariants)
  }

  test("loci set parsing with contig lengths") {
    ==(
      makeLociSet(
        "chr1,chr2,chr17,chr2:3-5,chr20:10-20",
        "chr1" → 10,
        "chr2" → 20,
        "chr17" → 12,
        "chr20" → 5000
      )
      .toString,
      "chr1:0-10,chr2:0-20,chr17:0-12,chr20:10-20"
    )
  }

  test("parse half-open interval") {
    ==(makeLociSet("chr1:10000-", "chr1" → 20000).toString, "chr1:10000-20000")
  }

  test("loci set single contig iterator basic") {
    val set = lociSet("chr1:20-25,chr1:15-17,chr1:40-43,chr1:40-42,chr1:5-5,chr2:5-6,chr2:6-7,chr2:2-4")
    ==(set("chr1").iterator.toSeq, Seq(15, 16, 20, 21, 22, 23, 24, 40, 41, 42))
    ==(set("chr2").iterator.toSeq, Seq(2, 3, 5, 6))

    val iter1 = set("chr1").iterator
    ==(iter1.hasNext, true)
    ==(iter1.head, 15)
    ==(iter1.next(), 15)
    ==(iter1.head, 16)
    ==(iter1.next(), 16)
    ==(iter1.head, 20)
    ==(iter1.next(), 20)
    ==(iter1.head, 21)
    iter1.skipTo(23)
    ==(iter1.next(), 23)
    ==(iter1.head, 24)
    iter1.skipTo(38)
    ==(iter1.head, 40)
    ==(iter1.hasNext, true)
    iter1.skipTo(100)
    ==(iter1.hasNext, false)
  }

  test("loci set single contig iterator: test that skipTo implemented efficiently.") {
    val set = lociSet("chr1:2-3,chr1:10-15,chr1:100-100000000000")

    val iter1 = set("chr1").iterator
    ==(iter1.hasNext, true)
    ==(iter1.head, 2)
    ==(iter1.next(), 2)
    ==(iter1.next(), 10)
    ==(iter1.next(), 11)

    val sixBillion = Locus(6000000000L)
    iter1.skipTo(sixBillion)  // will hang if it steps through each locus.
    ==(iter1.next(), sixBillion)
    ==(iter1.next(), sixBillion.next)
    ==(iter1.hasNext, true)

    val hundredBillion = Locus(100000000000L)
    val iter2 = set("chr1").iterator
    iter2.skipTo(hundredBillion)
    ==(iter2.hasNext, false)

    val iter3 = set("chr1").iterator
    iter3.skipTo(hundredBillion.prev)
    ==(iter3.hasNext, true)
    ==(iter3.next(), 100000000000L - 1)
    ==(iter3.hasNext, false)
  }

  test("take") {
    val set = lociSet("chr1:100-200,chr2:30-40,chr3:50-51,chr4:1000-1100")

    set.take(10) should be(
      (
        lociSet("chr1:100-110"),
        lociSet("chr1:110-200,chr2:30-40,chr3:50-51,chr4:1000-1100")
      )
    )

    set.take(0) should be(
      (
        lociSet(""),
        set
      )
    )

    set.take(200) should be(
      (
        lociSet("chr1:100-200,chr2:30-40,chr3:50-51,chr4:1000-1089"),
        lociSet("chr4:1089-1100")
      )
    )
  }
}
