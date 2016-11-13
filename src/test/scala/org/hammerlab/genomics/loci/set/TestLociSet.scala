package org.hammerlab.genomics.loci.set

import org.hammerlab.genomics.loci.parsing.ParsedLoci

object TestLociSet {
  def apply(parsedLoci: ParsedLoci): LociSet = LociSet(parsedLoci, Map())
  def apply(lociStr: String): LociSet = apply(ParsedLoci(lociStr))
}
