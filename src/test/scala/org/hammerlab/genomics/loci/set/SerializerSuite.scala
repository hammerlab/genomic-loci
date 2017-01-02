package org.hammerlab.genomics.loci.set

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream }

import org.apache.spark.broadcast.Broadcast
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.{ ContigNameUtil, LocusUtil }
import org.hammerlab.spark.test.suite.{ KryoSparkSuite, SparkSerialization }

import scala.collection.mutable

class SerializerSuite
  extends KryoSparkSuite(classOf[Registrar], referenceTracking = true)
    with SparkSerialization
    with LocusUtil
    with LociSetUtil
    with Serializable {

  import Helpers._

  // "a closure that includes a LociSet" parallelizes some Range[Long]s.
  kryoRegister(classOf[Range])
  kryoRegister(classOf[Array[Int]])

  // "make an RDD[LociSet] and an RDD[Contig]" collects some Strings.
  kryoRegister(classOf[Array[String]])

  kryoRegister(classOf[mutable.WrappedArray.ofRef[_]])

  test("make an RDD[LociSet]") {

    val sets =
      List[LociSet](
        "",
        "empty:20-20,empty2:30-30",
        "20:100-200",
        "21:300-400",
        "with_dots._and_..underscores11:900-1000",
        "X:5-17,X:19-22,Y:50-60",
        "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120"
      )

    val rdd = sc.parallelize(sets)
    val result = rdd.map(_.toString).collect.toSeq
    result should ===(sets.map(_.toString))
  }

  test("make an RDD[LociSet], and an RDD[Contig]") {
    val sets =
      List[LociSet](
        "",
        "empty:20-20,empty2:30-30",
        "20:100-200",
        "21:300-400",
        "X:5-17,X:19-22,Y:50-60",
        "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120"
      )

    val rdd = sc.parallelize(sets)

    val result =
      rdd
        .map(Helpers.lociSetMapTask)
        .collect
        .toSeq

    result should ===(sets.map(_("20").toString))
  }

  test("a closure that includes a LociSet") {
    val set: LociSet = "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10"
    val setBC = sc.broadcast(set)
    val rdd = sc.parallelize(0 until 1000)
    val result = rdd.filter(lociSetFilterTask(setBC)).collect
    result should ===(100 until 200)
  }

  test("java serialization") {
    val loci: LociSet = "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10"

    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)

    oos.writeObject(loci)
    oos.close()

    val bytes = baos.toByteArray
    val bais = new ByteArrayInputStream(bytes)
    val ois = new ObjectInputStream(bais)

    val loci2 = ois.readObject().asInstanceOf[LociSet]

    loci2 should ===(loci)
  }

}

object Helpers
  extends LocusUtil
    with ContigNameUtil {

  /**
   * Isolate this method in its own object because otherwise ClosureCleaner will attempt to Java-serialize the enclosing
   * [[org.hammerlab.test.Suite]], which errors due to a non-serializable [[org.scalatest.Assertions]].AssertionsHelper
   * member inherited from [[org.scalatest.FunSuite]]. See https://github.com/scalatest/scalatest/issues/1013.
   */
  def lociSetMapTask(set: LociSet): String = {
    set("21").contains(5)
    // no op
    val ranges = set("21").ranges // no op
    set("20").toString
  }

  def lociSetFilterTask(setBC: Broadcast[LociSet]) =
    (i: Int) ⇒
      setBC.value("chr21").contains(i)
}
