package org.hammerlab.genomics.loci.iterator

import org.hammerlab.genomics.reference.Locus
import org.hammerlab.magic.iterator.SimpleBufferedIterator

/**
 *
 * @param lociObjs1
 * @param lociObjs2
 * @tparam T
 */
class MergeLociIterator[+T](lociObjs1: BufferedIterator[(Locus, T)],
                            lociObjs2: BufferedIterator[(Locus, T)])
  extends SimpleBufferedIterator[(Locus, T)] {

  override def _advance: Option[(Locus, T)] = {
    (lociObjs1.hasNext, lociObjs2.hasNext) match {
      case (false, false) => None
      case ( true, false) => Some(lociObjs1.next())
      case (false,  true) => Some(lociObjs2.next())
      case ( true,  true) =>
        val (locus1, _) = lociObjs1.head
        val (locus2, _) = lociObjs2.head

        if (locus1 < locus2)
          Some(lociObjs1.next())
        else if (locus1 > locus2)
          Some(lociObjs2.next())
        else {
          lociObjs2.next()
          Some(lociObjs1.next())
        }
    }
  }
}
