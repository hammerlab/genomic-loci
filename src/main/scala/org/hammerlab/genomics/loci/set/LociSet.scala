package org.hammerlab.genomics.loci.set

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{ Input, Output }
import htsjdk.samtools.util.{ Interval ⇒ HTSJDKInterval }
import org.hammerlab.genomics.loci.parsing.{ All, LociRanges, ParsedLoci, Range }
import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.genomics.reference.{ ContigLengths, ContigName, Interval, Locus, NumLoci, Region }
import org.hammerlab.kryo
import org.hammerlab.strings.TruncatedToString

import scala.collection.SortedMap
import scala.collection.immutable.TreeMap

/**
 * An immutable collection of genomic regions on any number of contigs.
 *
 * Used, for example, to keep track of what loci to call variants at.
 *
 * Since contiguous genomic intervals are a common case, this is implemented with sets of (start, end) intervals.
 *
 * All intervals are half open: inclusive on start, exclusive on end.
 *
 * @param map A map from contig-name to Contig, which is a set or genomic intervals as described above.
 */
case class LociSet(private val map: SortedMap[ContigName, Contig])
  extends TruncatedToString {

  /** The contigs included in this LociSet with a nonempty set of loci. */
  @transient lazy val contigs = map.values.toArray

  /** The number of loci in this LociSet. */
  @transient lazy val count: NumLoci = contigs.map(_.count).sum

  def isEmpty = map.isEmpty
  def nonEmpty = map.nonEmpty

  /** Given a contig name, returns a [[Contig]] giving the loci on that contig. */
  def apply(contigName: ContigName): Contig = map.getOrElse(contigName, Contig(contigName))

  /** Build a truncate-able toString() out of underlying contig pieces. */
  def stringPieces: Iterator[String] = contigs.iterator.flatMap(_.stringPieces)

  def intersects(region: Region): Boolean =
    apply(region.contigName).intersects(region.start, region.end)

  /**
   * Split the LociSet into two sets, where the first one has `numToTake` loci, and the second one has the
   * remaining loci.
   *
   * @param numToTake number of elements to take. Must be <= number of elements in the map.
   */
  def take(numToTake: NumLoci): (LociSet, LociSet) = {
    assume(numToTake <= count, s"Can't take $numToTake loci from a set of size $count.")

    // Optimize for taking none or all:
    if (numToTake == NumLoci(0)) {
      (LociSet(), this)
    } else if (numToTake == count) {
      (this, LociSet())
    } else {

      val first = new Builder
      val second = new Builder
      var remaining = numToTake
      var doneTaking = false

      for {
        contig <- contigs
      } {
        if (doneTaking) {
          second.add(contig)
        } else if (contig.count < remaining) {
          first.add(contig)
          remaining -= contig.count
        } else {
          val (takePartialContig, remainingPartialContig) = contig.take(remaining)
          first.add(takePartialContig)
          second.add(remainingPartialContig)
          doneTaking = true
        }
      }

      val (firstSet, secondSet) = (first.result, second.result)
      ==(firstSet.count, numToTake)
      ==(firstSet.count + secondSet.count, count)
      (firstSet, secondSet)
    }
  }


  /**
   * Build a collection of HTSJDK Intervals which are closed [start, end], 1-based intervals
   */
  def toHtsJDKIntervals: List[HTSJDKInterval] =
    map
      .keys
      .flatMap(
        contig ⇒
          apply(contig)
            .ranges
            // We add 1 to the start to move to 1-based coordinates
            // Since the `Interval` end is inclusive, we are adding and subtracting 1, no-op
            .map(interval ⇒
              new HTSJDKInterval(
                contig.name,
                interval.start.locus.toInt + 1,
                interval.end.locus.toInt
              )
          )
      )
      .toList
}

object LociSet {
  /** An empty LociSet. */
  def apply(): LociSet = LociSet(TreeMap.empty[ContigName, Contig])

  def all(contigLengths: ContigLengths) = LociSet(All, contigLengths)

  /**
   * These constructors build a LociSet directly from Contigs.
   *
   * They operate on an Iterator so that transformations to the data happen in one pass.
   */
  def fromContigs(contigs: Iterable[Contig]): LociSet = fromContigs(contigs.iterator)
  def fromContigs(contigs: Iterator[Contig]): LociSet =
    LociSet(
      TreeMap(
        contigs
          .filterNot(_.isEmpty)
          .map(contig ⇒ contig.name → contig)
          .toSeq: _*
      )
    )

  def apply(regions: Iterable[Region])(implicit f: Factory): LociSet =
    LociSet.fromContigs(
      (for {
        Region(contigName, start, end) ← regions
        if start != end
        range = Interval(start, end)
      } yield
        contigName → range
      )
      .groupBy(_._1)
      .mapValues(_.map(_._2))
      .map(Contig(_))
    )

  def apply(loci: ParsedLoci, contigLengths: ContigLengths)(implicit f: Factory): LociSet =
    LociSet(
      loci match {
        case All ⇒
          for {
            (contig, length) ← contigLengths
          } yield
            Region(contig, Locus(0), Locus(length))
        case LociRanges(ranges) ⇒
          for {
            Range(contigName, start, endOpt) ← ranges
            contigLengthOpt = contigLengths.get(contigName)
          } yield
            (endOpt, contigLengthOpt) match {
              case (Some(end), Some(contigLength)) if end > Locus(contigLength) ⇒
                throw new IllegalArgumentException(
                  s"Invalid range $start-${endOpt.get} for contig '$contigName' which has length $contigLength"
                )
              case (Some(end), _) ⇒
                Region(contigName, start, end)
              case (_, Some(contigLength)) ⇒
                Region(contigName, start, Locus(contigLength))
              case _ ⇒
                throw new IllegalArgumentException(
                  s"No such contig: $contigName. Valid contigs: ${contigLengths.keys.mkString(", ")}"
                )
            }
      }
    )

  import org.hammerlab.kryo._

  // We just serialize the underlying contigs, which contain their names which are the string keys of LociSet.map.
  implicit val serializer: Serializer[LociSet] =
    Serializer(
      (kryo, input) ⇒ {
        val contigs = kryo.readObject(input, classOf[Array[Contig]])
        LociSet.fromContigs(contigs)
      },
      (kryo, out, obj) ⇒ kryo.writeObject(out, obj.contigs)
    )

  implicit val alsoRegister: AlsoRegister[LociSet] = AlsoRegister(arr[Contig])
}
