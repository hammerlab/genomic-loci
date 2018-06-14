package org.hammerlab.genomics.loci.set

import org.hammerlab.cmp.CanEq.Cmp
import org.hammerlab.cmp.Cmp
import org.hammerlab.genomics.loci
import org.hammerlab.genomics.reference.Interval

trait cmps
  extends loci.cmps {
  implicit def cmpSetContig(implicit cmp: Cmp[Iterator[Interval]]): Cmp.Aux[Contig, cmp.Diff] = Cmp.by[Iterator[Interval], Contig](_.ranges)(cmp)
  implicit def lociSetCmp(implicit cmp: Cmp[Seq[Contig]]): Cmp.Aux[LociSet, cmp.Diff] = Cmp.by[Seq[Contig], LociSet](_.contigs)(cmp)
}
