package org.hammerlab.genomics.loci.set

import com.google.common.collect.Range.closedOpen
import com.google.common.collect.{ RangeSet, TreeRangeSet, Range ⇒ JRange }
import org.hammerlab.genomics.loci.iterator.LociIterator
import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus, NumLoci, Region }
import org.hammerlab.strings.TruncatedToString

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * A set of loci on a contig, stored/manipulated as loci ranges.
 */
case class Contig(name: ContigName,
                  rangeSet: RangeSet[Locus])(implicit factory: Factory)
  extends TruncatedToString {

  import Contig.lociRange

  /**
   * [[RangeSet]] is not [[Serializable]], and we delegate work-around serialization logic to [[SerializableContig]] to
   * avoid the requirement for a 0-arg constructor here.
   */
  protected def writeReplace: Object =
    SerializableContig(this)

  /** Is the given locus contained in this set? */
  def contains(locus: Locus): Boolean = rangeSet.contains(locus)

  @transient private lazy val jranges = rangeSet.asRanges()
  @transient lazy val numRanges = jranges.size()

  /** This set as a regular scala array of ranges. */
  def ranges: Iterator[Interval] =
    jranges
      .iterator()
      .asScala
      .map(range => Interval(range.lowerEndpoint(), range.upperEndpoint()))

  def regions: Iterator[Region] = ranges.map(range ⇒ Region(name, range))

  /** Is this contig empty? */
  def isEmpty: Boolean = rangeSet.isEmpty

  def nonEmpty: Boolean = !isEmpty

  /** Iterator through loci on this contig, sorted. */
  def iterator = new LociIterator(ranges.buffered)

  /** Number of loci on this contig. */
  @transient lazy val count: NumLoci = ranges.map(_.length).sum

  /** Returns whether a given genomic region overlaps with any loci on this contig. */
  def intersects(start: Locus, end: Locus): Boolean = !rangeSet.subRangeSet(lociRange(start, end)).isEmpty

  /**
   * Make two new Contigs: one with the first @numToTake loci from this Contig, and the second with the rest.
   *
   * Used by LociSet.take.
   */
  private[set] def take(numToTake: NumLoci): (Contig, Contig) = {
    val firstRanges = ArrayBuffer[Interval]()
    val secondRanges = ArrayBuffer[Interval]()

    var remaining = numToTake
    var doneTaking = false
    for {
      range ← ranges
    } {
      if (doneTaking) {
        secondRanges.append(range)
      } else if (range.length < numToTake) {
        firstRanges.append(range)
        remaining -= range.length
      } else {
        firstRanges.append(Interval(range.start, range.start + remaining))
        secondRanges.append(Interval(range.start + remaining, range.end))
        doneTaking = true
      }
    }

    (Contig(name, firstRanges), Contig(name, secondRanges))
  }

  /**
   * Iterator over string representations of each range in the map, used to assemble (possibly truncated) .toString()
   * output.
   */
  def stringPieces = regions.map(_.toString)
}

object Contig {
  // Empty-contig constructor, for convenience.
  def apply(name: ContigName)(implicit f: Factory): Contig = Contig(name, TreeRangeSet.create[Locus]())

  // Constructors that make a Contig from its name and some ranges.
  def apply(tuple: (ContigName, Iterable[Interval]))(implicit f: Factory): Contig = Contig(tuple._1, tuple._2)
  def apply(name: ContigName, ranges: Iterable[Interval])(implicit f: Factory): Contig = apply(name, ranges.iterator)
  def apply(name: ContigName, ranges: Iterator[Interval])(implicit f: Factory): Contig =
    Contig(
      name,
      {
        val rangeSet = TreeRangeSet.create[Locus]()
        for {
          Interval(start, end) ← ranges
        } {
          rangeSet.add(lociRange(start, end))
        }
        rangeSet
      }
    )

  def lociRange(start: Locus, end: Locus): JRange[Locus] = closedOpen[Locus](start, end)
}
