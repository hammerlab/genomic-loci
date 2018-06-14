package org.hammerlab.genomics.loci.map

import org.hammerlab.cmp.CanEq.Cmp
import org.hammerlab.cmp.Cmp
import org.hammerlab.genomics.loci.{ map, set }

trait cmps
  extends set.cmps {
  implicit def cmpMapContig[T](implicit cmp: Cmp[Map[T, set.Contig]]): Cmp.Aux[map.Contig[T], cmp.Diff] = Cmp.by[Map[T, set.Contig], map.Contig[T]](_.inverse)(cmp)
  implicit def lociMapCmp[T](implicit cmp: Cmp[Seq[map.Contig[T]]]): Cmp.Aux[LociMap[T], cmp.Diff] = Cmp.by[Seq[map.Contig[T]], LociMap[T]](_.contigs)(cmp)
}
