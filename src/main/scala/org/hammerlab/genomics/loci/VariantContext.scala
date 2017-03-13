package org.hammerlab.genomics.loci

import htsjdk.variant.variantcontext.{VariantContext => HTSJDKVariantContext}
import org.hammerlab.genomics.reference.{ContigName, Locus}

object VariantContext {
  def unapply(vc: HTSJDKVariantContext): Option[(ContigName, Locus, Locus)] =
    Some(
      (
        vc.getContig,
        Locus(vc.getStart - 1L),
        Locus(vc.getEnd.toLong)
      )
    )
}
