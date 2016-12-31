package org.hammerlab.genomics.loci.map

import com.google.common.collect.{ RangeMap, TreeRangeMap, Range ⇒ JRange }
import org.hammerlab.genomics.loci.set.{ Contig ⇒ LociSetContig }
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus, NumLoci, Region }
import org.hammerlab.strings.TruncatedToString
import org.hammerlab.genomics.reference.Interval.orderByStart

import scala.collection.JavaConversions._
import scala.collection.immutable.{ SortedMap, TreeMap }
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * A map from loci to instances of an arbitrary type where the loci are all on the same contig.
 *
 * @param name The contig name
 * @param rangeMap The range map of loci intervals -> values.
 */
case class Contig[T](name: ContigName, private val rangeMap: RangeMap[Locus, T]) extends TruncatedToString {

  import Contig.lociRange

  /**
   * Get the value associated with the given locus. Returns Some(value) if the given locus is in this map, None
   * otherwise.
   */
  def get(locus: Locus): Option[T] = {
    Option(rangeMap.get(locus))
  }

  /**
   * Given a loci interval, return the set of all values mapped to by any loci in the interval.
   */
  def getAll(interval: Interval, halfWindowSize: Int = 0): Set[T] =
    getAll(interval.start, interval.end, halfWindowSize)
  def getAll(start: Locus, end: Locus): Set[T] = getAll(start, end, halfWindowSize = 0)
  def getAll(start: Locus, end: Locus, halfWindowSize: Int): Set[T] =
    rangeMap
      .subRangeMap(lociRange(start.locus - halfWindowSize, end.locus + halfWindowSize))
      .asMapOfRanges
      .values
      .toSet

  /** This map as a regular scala immutable map from exclusive numeric ranges to values. */
  lazy val asMap: SortedMap[Interval, T] =
    TreeMap(
      (for {
        (range, value) <- rangeMap.asMapOfRanges.toSeq
      } yield
        Interval(range.lowerEndpoint(), range.upperEndpoint()) -> value
      ): _*
    )(orderByStart)

  def asRegionsMap: Iterator[(Region, T)] =
    for {
      (interval, value) ← asMap.iterator
    } yield
      Region(name, interval) → value

  /**
   * Map from each value found in this Contig to a LociSet Contig representing the loci that map to that value.
   */

  lazy val inverse: Map[T, LociSetContig] = {
    val map = mutable.HashMap[T, ArrayBuffer[Interval]]()
    for {
      (range, value) <- asMap
    } {
      map
        .getOrElseUpdate(value, ArrayBuffer())
        .append(range)
    }
    map.mapValues(ranges => LociSetContig(name, ranges)).toMap
  }

  /** Number of loci on this contig; exposed only to LociMap. */
  private[map] lazy val count: NumLoci = asMap.keysIterator.map(_.length).sum

  /**
   * Iterator over string representations of each range in the map.
   */
  def stringPieces =
    for {
      (region, value) ← asRegionsMap
    } yield
      s"$region=$value"
}

object Contig {

  def apply[T](name: ContigName): Contig[T] = Contig(name, TreeRangeMap.create[Locus, T]())

  /** Convenience constructors for making a Contig from a name and some loci ranges. */
  def apply[T](tuple: (ContigName, Iterable[(Locus, Locus, T)])): Contig[T] = apply(tuple._1, tuple._2)

  def apply[T](name: ContigName, ranges: Iterable[(Locus, Locus, T)]): Contig[T] = {
    val mutableRangeMap = TreeRangeMap.create[Locus, T]()
    ranges.foreach { item =>
      var (start, end, value) = item

      // If there is an existing entry associated *with the same value* in the map immediately before the range
      // we're adding, we coalesce the two ranges by setting our start to be its start.
      val existingStart = mutableRangeMap.getEntry(start.prev)
      if (existingStart != null && existingStart.getValue == value) {
        assert(existingStart.getKey.lowerEndpoint < start)
        start = existingStart.getKey.lowerEndpoint
      }

      // Likewise for the end of the range.
      val existingEnd = mutableRangeMap.getEntry(end)
      if (existingEnd != null && existingEnd.getValue == value) {
        assert(existingEnd.getKey.upperEndpoint > end)
        end = existingEnd.getKey.upperEndpoint
      }

      mutableRangeMap.put(lociRange(start, end), value)
    }

    Contig(name, mutableRangeMap)
  }

  def lociRange(start: Locus, end: Locus): JRange[Locus] = JRange.closedOpen[Locus](start, end)
}
