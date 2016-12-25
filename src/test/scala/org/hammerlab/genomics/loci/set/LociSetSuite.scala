package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.test.TestLociSet
import org.hammerlab.genomics.reference.test.ContigLengthsUtil._
import org.hammerlab.genomics.reference.test.LocusUtil._
import org.hammerlab.genomics.reference.{ ContigLengths, ContigName, Locus, NumLoci }
import org.hammerlab.spark.test.suite.KryoSerializerSuite

import scala.collection.mutable

class LociSetSuite extends KryoSerializerSuite(classOf[Registrar]) {

  // "loci set invariants" collects some LociSets
  kryoRegister(classOf[mutable.WrappedArray.ofRef[_]])

  def makeLociSet(str: String, lengths: (ContigName, NumLoci)*): LociSet =
    LociSet(ParsedLoci(str), lengths.toMap)

  test("properties of empty LociSet") {
    val empty = LociSet()
    empty.contigs should have size 0
    empty.count === 0
    empty === TestLociSet("")
    val empty2 = TestLociSet("empty1:30-30,empty2:40-40")
    empty === empty2
  }

  test("count, containment, intersection testing of a loci set") {
    val set = TestLociSet("chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10")
    set.contigs.map(_.name) === Seq("chr20", "chr21")
    set.count === 135
    set.onContig("chr20").contains(110) === true
    set.onContig("chr20").contains(100) === true
    set.onContig("chr20").contains(99) === false
    set.onContig("chr20").contains(120) === false
    set.onContig("chr20").contains(119) === true
    set.onContig("chr20").count === 35
    set.onContig("chr20").intersects(0, 5) === true
    set.onContig("chr20").intersects(0, 1) === true
    set.onContig("chr20").intersects(0, 0) === false
    set.onContig("chr20").intersects(7, 8) === true
    set.onContig("chr20").intersects(9, 11) === true
    set.onContig("chr20").intersects(11, 18) === true
    set.onContig("chr20").intersects(18, 19) === false
    set.onContig("chr20").intersects(14, 80) === true
    set.onContig("chr20").intersects(15, 80) === false
    set.onContig("chr20").intersects(120, 130) === false
    set.onContig("chr20").intersects(119, 130) === true

    set.onContig("chr21").contains(99) === false
    set.onContig("chr21").contains(100) === true
    set.onContig("chr21").contains(200) === false
    set.onContig("chr21").count === 100
    set.onContig("chr21").intersects(110, 120) === true
    set.onContig("chr21").intersects(90, 120) === true
    set.onContig("chr21").intersects(150, 200) === true
    set.onContig("chr21").intersects(150, 210) === true
    set.onContig("chr21").intersects(200, 210) === false
    set.onContig("chr21").intersects(201, 210) === false
    set.onContig("chr21").intersects(90, 100) === false
    set.onContig("chr21").intersects(90, 101) === true
    set.onContig("chr21").intersects(90, 95) === false
    set.onContig("chr21").iterator.toSeq === (100 until 200)
  }

  test("single loci parsing") {
    val set = TestLociSet("chr1:10000")
    set.count === 1
    set.onContig("chr1").contains( 9999) === false
    set.onContig("chr1").contains(10000) === true
    set.onContig("chr1").contains(10001) === false
  }

  test("loci set invariants") {
    val sets = List(
      "",
      "empty:20-20,empty2:30-30",
      "20:100-200",
      "with_dots.and_underscores..2:100-200",
      "21:300-400",
      "X:5-17,X:19-22,Y:50-60",
      "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120"
    ).map(TestLociSet(_))

    def checkInvariants(set: LociSet): Unit = {
      set should not be null
      set.toString should not be null
      withClue("invariants for: '%s'".format(set.toString)) {
        TestLociSet(set.toString) === set
        TestLociSet(set.toString).toString === set.toString
        set === set

        // Test serialization. We hit all sorts of null pointer exceptions here at one point, so we are paranoid about
        // checking every pointer.
        val parallelized = sc.parallelize(List(set))
        val collected = parallelized.collect()
        val result = collected(0)
        result === set
      }
    }
    sets.foreach(checkInvariants)
  }

  test("loci set parsing with contig lengths") {
    makeLociSet(
      "chr1,chr2,17,chr2:3-5,chr20:10-20",
      "chr1" -> 10,
      "chr2" -> 20,
      "17" -> 12,
      "chr20" -> 5000
    )
    .toString === "17:0-12,chr1:0-10,chr2:0-20,chr20:10-20"
  }

  test("parse half-open interval") {
    makeLociSet("chr1:10000-", "chr1" -> 20000).toString === "chr1:10000-20000"
  }

  test("loci set single contig iterator basic") {
    val set = TestLociSet("chr1:20-25,chr1:15-17,chr1:40-43,chr1:40-42,chr1:5-5,chr2:5-6,chr2:6-7,chr2:2-4")
    set.onContig("chr1").iterator.toSeq === Seq(15, 16, 20, 21, 22, 23, 24, 40, 41, 42)
    set.onContig("chr2").iterator.toSeq === Seq(2, 3, 5, 6)

    val iter1 = set.onContig("chr1").iterator
    iter1.hasNext === true
    iter1.head === 15
    iter1.next() === 15
    iter1.head === 16
    iter1.next() === 16
    iter1.head === 20
    iter1.next() === 20
    iter1.head === 21
    iter1.skipTo(23)
    iter1.next() === 23
    iter1.head === 24
    iter1.skipTo(38)
    iter1.head === 40
    iter1.hasNext === true
    iter1.skipTo(100)
    iter1.hasNext === false
  }

  test("loci set single contig iterator: test that skipTo implemented efficiently.") {
    val set = TestLociSet("chr1:2-3,chr1:10-15,chr1:100-100000000000")

    val iter1 = set.onContig("chr1").iterator
    iter1.hasNext === true
    iter1.head === 2
    iter1.next() === 2
    iter1.next() === 10
    iter1.next() === 11
    iter1.skipTo(6000000000L)  // will hang if it steps through each locus.
    iter1.next() === 6000000000L
    iter1.next() === 6000000001L
    iter1.hasNext === true

    val iter2 = set.onContig("chr1").iterator
    iter2.skipTo(100000000000L)
    iter2.hasNext === false

    val iter3 = set.onContig("chr1").iterator
    iter3.skipTo(100000000000L - 1)
    iter3.hasNext === true
    iter3.next() === 100000000000L - 1
    iter3.hasNext === false
  }
}
