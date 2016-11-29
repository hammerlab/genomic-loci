package org.hammerlab.genomics.reference

/**
 * Trait for objects that are associated with an interval on a genomic contig.
 */
trait Region
  extends HasContig
    with Interval {
  /**
   * Does the region overlap the given locus, with halfWindowSize padding?
   */
  def overlapsLocus(locus: Locus, halfWindowSize: Int = 0): Boolean = {
    start - halfWindowSize <= locus && end + halfWindowSize > locus
  }

  /**
   * Does the region overlap another reference region
   *
   * @param other another region on the genome
   * @return True if the the regions overlap
   */
  def overlaps(other: Region): Boolean = {
    other.contigName == contigName && (overlapsLocus(other.start) || other.overlapsLocus(start))
  }

  def regionStr: String = s"$contigName:[$start-$end)"
}

object Region {
  implicit def intraContigPartialOrdering[R <: Region] =
    new PartialOrdering[R] {
      override def tryCompare(x: R, y: R): Option[Int] = {
        if (x.contigName == y.contigName)
          Some(x.start.compare(y.start))
        else
          None
      }

      override def lteq(x: R, y: R): Boolean = {
        x.contigName == y.contigName && x.start <= y.start
      }
    }

  def apply(contigName: ContigName, start: Locus, end: Locus): Region =
    RegionImpl(contigName, start, end)

  def unapply(region: Region): Option[(ContigName, Locus, Locus)] =
    Some(
      region.contigName,
      region.start,
      region.end
    )
}

private case class RegionImpl(contigName: ContigName, start: Locus, end: Locus) extends Region
