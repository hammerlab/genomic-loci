package org.hammerlab.genomics.reference.test

trait RegionsUtil {
  def makeRegions(reads: Seq[(String, Int, Int, Int)]): BufferedIterator[TestRegion] =
    (for {
      (contig, start, end, num) <- reads
      i <- 0 until num
    } yield
      TestRegion(contig, start, end)
    ).iterator.buffered
}
