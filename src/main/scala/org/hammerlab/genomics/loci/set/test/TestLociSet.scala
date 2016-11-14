package org.hammerlab.genomics.loci.set.test

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.LociSet

/**
 * Convenience wrapper for creating dummy LociSets in tests; we could publish this separately from the main JAR, but
 * it's just one tiny class for now so we leave it bundled.
 */
object TestLociSet {
  def apply(parsedLoci: ParsedLoci): LociSet = LociSet(parsedLoci, Map())
  def apply(lociStr: String): LociSet = apply(ParsedLoci(lociStr))
}
