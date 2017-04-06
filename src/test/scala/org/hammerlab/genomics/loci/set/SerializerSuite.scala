package org.hammerlab.genomics.loci.set

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream }

import org.apache.spark.broadcast.Broadcast
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.genomics.reference.{ Locus, PermissiveRegistrar }
import org.hammerlab.genomics.reference.test.{ ClearContigNames, LenientContigNameConversions }
import org.hammerlab.genomics.reference.test.LociConversions._
import org.hammerlab.spark.test.suite.{ KryoSparkSuite, SparkSerialization }

import scala.collection.mutable

class SerializerSuite
  extends KryoSparkSuite(classOf[Registrar], referenceTracking = true)
    with SparkSerialization
    with LenientContigNameConversions
    with ClearContigNames
    with LociSetUtil
    with Serializable {

  import Helpers._

  register(
    // "a closure that includes a LociSet" parallelizes some Range[Long]s.
    classOf[Range],
    classOf[Array[Locus]],

    // "make an RDD[LociSet] and an RDD[Contig]" collects some Strings.
    classOf[Array[String]],

    classOf[mutable.WrappedArray.ofRef[_]],

    PermissiveRegistrar
  )

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
        .map(mapTask)
        .collect
        .toSeq

    result should ===(sets.map(_("20").toString))
  }


  test("a closure that includes a LociSet") {
    val set: LociSet = "chr21:100-200,chr20:0-10,chr20:8-15,chr20:100-120,empty:10-10"
    val setBC = sc.broadcast(set)
    val rdd = sc.parallelize[Locus]((0 until 1000).toSeq)
    val result = rdd.filter(filterTask(setBC)).collect
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

object Helpers {

  /**
   * Isolate this method in its own object because otherwise ClosureCleaner will attempt to Java-serialize the enclosing
   * [[org.hammerlab.test.Suite]], which errors due to a non-serializable [[org.scalatest.Assertions]].AssertionsHelper
   * member inherited from [[org.scalatest.FunSuite]]. See https://github.com/scalatest/scalatest/issues/1013.
   */
  def mapTask(implicit factory: Factory) =
    (set: LociSet) ⇒ {
      set("21").contains(5)
      // no op
      val _ = set("21").ranges // no op
      set("20").toString
    }

  def filterTask(setBC: Broadcast[LociSet])(implicit factory: Factory) =
    (locus: Locus) ⇒
      setBC.value("chr21").contains(locus)
}
