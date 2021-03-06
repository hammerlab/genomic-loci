package org.hammerlab.genomics.loci.map

import org.hammerlab.genomics.loci.set.LociSet
import org.hammerlab.genomics.reference.{ ContigName, Interval, Locus }

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** Helper class for building a LociMap */
private[loci] class Builder[T] {
  private val data = new mutable.HashMap[ContigName, ArrayBuffer[(Locus, Locus, T)]]()

  def +=(contig: ContigName, start: Locus, end: Locus, value: T): Builder[T] =
    put(contig, start, end, value)

  /** Set the value at the given locus range in the LociMap under construction. */
  def put(contig: ContigName, start: Locus, end: Locus, value: T): Builder[T] = {
    assume(end >= start)
    if (end > start) {
      data
        .getOrElseUpdate(contig, ArrayBuffer())
        .append((start, end, value))
    }
    this
  }

  /** Set the value for all loci in the given LociSet to the specified value in the LociMap under construction. */
  def put(loci: LociSet, value: T): Builder[T] = {
    for {
      contig ← loci.contigs
      Interval(start, end) ← contig.ranges
    } {
      put(contig.name, start, end, value)
    }
    this
  }

  /** Build the result. */
  def result: LociMap[T] = LociMap.fromContigs(data.map(Contig(_)))
}

