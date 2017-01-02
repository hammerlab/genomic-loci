package org.hammerlab.genomics.loci.set.test

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.LociSet
import org.hammerlab.genomics.reference.ContigLengths

/**
 * Convenience wrapper for creating dummy LociSets in tests; we could publish this separately from the main JAR, but
 * it's just one tiny class for now so we leave it bundled.
 */
trait LociSetUtil {
  implicit def lociSetFromParsedLoci(parsedLoci: ParsedLoci): LociSet = LociSet(parsedLoci, ContigLengths.empty)
  implicit def lociSetFromString(lociStr: String): LociSet = ParsedLoci(lociStr)
  def lociSet(lociStr: String): LociSet = lociStr
  def lociSet(parsedLoci: ParsedLoci): LociSet = parsedLoci
}

object LociSetUtil extends LociSetUtil
