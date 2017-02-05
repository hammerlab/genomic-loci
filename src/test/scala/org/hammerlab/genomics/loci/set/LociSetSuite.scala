package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.{ ClearContigNames, ContigLengthsUtil }
import org.hammerlab.genomics.reference.test.ContigNameConversions.toArray
import org.hammerlab.genomics.reference.test.LociConversions.{ intToLocus, toSeq }
import org.hammerlab.genomics.reference.{ ContigLengths, ContigName, Locus, NumLoci }
import org.hammerlab.spark.test.suite.KryoSparkSuite

import scala.collection.mutable

class LociSetSuite
  extends KryoSparkSuite(classOf[Registrar])
    with LociSetUtil
    with ClearContigNames
    with ContigLengthsUtil {

  import org.hammerlab.genomics.reference.ContigName.Normalization.Lenient

  // "loci set invariants" collects some LociSets
  register(classOf[mutable.WrappedArray.ofRef[_]])

  def makeLociSet(str: String, lengths: (ContigName, NumLoci)*): LociSet =
    LociSet(ParsedLoci(str), lengths.toMap)

  test("properties of empty LociSet") {
    val empty = LociSet()
    empty.contigs should have size 0
    empty.count should ===(0)
    empty should ===(lociSet(""))
    val empty2 = lociSet("empty1:30-30,empty2:40-40")
    empty should ===(empty2)
  }

  test("count, containment, intersection testing of a loci set") {
    val set = lociSet("chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10")
    set.contigs.map(_.name) should ===(Seq("chr20", "chr21"))
    set.count should ===(135)
    set("chr20").contains(110) should ===(true)
    set("chr20").contains(100) should ===(true)
    set("chr20").contains(99) should ===(false)
    set("chr20").contains(120) should ===(false)
    set("chr20").contains(119) should ===(true)
    set("chr20").count should ===(35)
    set("chr20").intersects(0, 5) should ===(true)
    set("chr20").intersects(0, 1) should ===(true)
    set("chr20").intersects(0, 0) should ===(false)
    set("chr20").intersects(7, 8) should ===(true)
    set("chr20").intersects(9, 11) should ===(true)
    set("chr20").intersects(11, 18) should ===(true)
    set("chr20").intersects(18, 19) should ===(false)
    set("chr20").intersects(14, 80) should ===(true)
    set("chr20").intersects(15, 80) should ===(false)
    set("chr20").intersects(120, 130) should ===(false)
    set("chr20").intersects(119, 130) should ===(true)

    set("chr21").contains(99) should ===(false)
    set("chr21").contains(100) should ===(true)
    set("chr21").contains(200) should ===(false)
    set("chr21").count should ===(100)
    set("chr21").intersects(110, 120) should ===(true)
    set("chr21").intersects(90, 120) should ===(true)
    set("chr21").intersects(150, 200) should ===(true)
    set("chr21").intersects(150, 210) should ===(true)
    set("chr21").intersects(200, 210) should ===(false)
    set("chr21").intersects(201, 210) should ===(false)
    set("chr21").intersects(90, 100) should ===(false)
    set("chr21").intersects(90, 101) should ===(true)
    set("chr21").intersects(90, 95) should ===(false)
    set("chr21").iterator.toSeq should ===(100 until 200)
  }

  test("single loci parsing") {
    val set = lociSet("chr1:10000")
    set.count should ===(1)
    set("chr1").contains( 9999) should ===(false)
    set("chr1").contains(10000) should ===(true)
    set("chr1").contains(10001) should ===(false)
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
        "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120"
      )
      .map(lociSet)

    def checkInvariants(set: LociSet): Unit = {
      set should not be null
      set.toString should not be null
      withClue("invariants for: '%s'".format(set.toString)) {
        lociSet(set.toString) should ===(set)
        lociSet(set.toString).toString should ===(set.toString)
        set should ===(set)

        // Test serialization. We hit all sorts of null pointer exceptions here at one point, so we are paranoid about
        // checking every pointer.
        val parallelized = sc.parallelize(List(set))
        val collected = parallelized.collect()
        val result = collected(0)
        result should ===(set)
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
    .toString should ===("chr1:0-10,chr2:0-20,17:0-12,chr20:10-20")
  }

  test("parse half-open interval") {
    makeLociSet("chr1:10000-", "chr1" -> 20000).toString should ===("chr1:10000-20000")
  }

  test("loci set single contig iterator basic") {
    val set = lociSet("chr1:20-25,chr1:15-17,chr1:40-43,chr1:40-42,chr1:5-5,chr2:5-6,chr2:6-7,chr2:2-4")
    set("chr1").iterator.toSeq should ===(Seq(15, 16, 20, 21, 22, 23, 24, 40, 41, 42))
    set("chr2").iterator.toSeq should ===(Seq(2, 3, 5, 6))

    val iter1 = set("chr1").iterator
    iter1.hasNext should ===(true)
    iter1.head should ===(15)
    iter1.next() should ===(15)
    iter1.head should ===(16)
    iter1.next() should ===(16)
    iter1.head should ===(20)
    iter1.next() should ===(20)
    iter1.head should ===(21)
    iter1.skipTo(23)
    iter1.next() should ===(23)
    iter1.head should ===(24)
    iter1.skipTo(38)
    iter1.head should ===(40)
    iter1.hasNext should ===(true)
    iter1.skipTo(100)
    iter1.hasNext should ===(false)
  }

  test("loci set single contig iterator: test that skipTo implemented efficiently.") {
    val set = lociSet("chr1:2-3,chr1:10-15,chr1:100-100000000000")

    val iter1 = set("chr1").iterator
    iter1.hasNext should ===(true)
    iter1.head should ===(2)
    iter1.next() should ===(2)
    iter1.next() should ===(10)
    iter1.next() should ===(11)
    iter1.skipTo(6000000000L)  // will hang if it steps through each locus.
    iter1.next() should ===(6000000000L)
    iter1.next() should ===(6000000001L)
    iter1.hasNext should ===(true)

    val iter2 = set("chr1").iterator
    iter2.skipTo(100000000000L)
    iter2.hasNext should ===(false)

    val iter3 = set("chr1").iterator
    iter3.skipTo(100000000000L - 1)
    iter3.hasNext should ===(true)
    iter3.next() should ===(100000000000L - 1)
    iter3.hasNext should ===(false)
  }
}
