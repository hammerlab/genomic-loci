package org.hammerlab.genomics.reference

case class Slice(contigName: ContigName,
                 start: Locus,
                 bases: Seq[Byte])
  extends Region {

  val end: Locus = start + bases.size

  private def locusToIndex(locus: Locus): Int = (locus - start).toInt

  def apply(locus: Locus): Byte =
    bases(locusToIndex(locus))

  def slice(sliceStart: Locus, sliceEnd: Locus = end): Seq[Byte] =
    bases.slice(
      locusToIndex(sliceStart),
      locusToIndex(sliceEnd)
    )
}
