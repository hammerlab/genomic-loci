package org.hammerlab.genomics.loci

import org.hammerlab.cmp.CanEq.Cmp
import org.hammerlab.cmp.Cmp
import org.hammerlab.genomics.reference.{ Interval, Locus }

trait cmps {
  // TODO: move this to reference repo
  implicit def cmpInterval(implicit cmp: Cmp[(Locus, Locus)]): Cmp.Aux[Interval, cmp.Diff] = Cmp.by[(Locus, Locus), Interval](i ⇒ (i.start, i.end))(cmp)
}
