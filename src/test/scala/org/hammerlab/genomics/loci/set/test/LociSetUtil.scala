package org.hammerlab.genomics.loci.set.test

import org.hammerlab.genomics.loci.parsing.ParsedLoci
import org.hammerlab.genomics.loci.set.LociSet
import org.hammerlab.genomics.reference.ContigLengths
import org.hammerlab.genomics.reference.ContigName.Factory
import org.scalatest.Suite

/**
 * Convenience wrapper for creating dummy LociSets in tests; we could publish this separately from the main JAR, but
 * it's just one tiny class for now so we leave it bundled.
 */
trait LociSetUtil {
  self: Suite ⇒
  implicit def parsedLociFromString(lociStr: String)(implicit factory: Factory): ParsedLoci = ParsedLoci(lociStr)
  implicit def lociSetFromParsedLoci(parsedLoci: ParsedLoci)(implicit factory: Factory): LociSet = LociSet(parsedLoci, ContigLengths.empty)
  implicit def lociSetFromString(lociStr: String)(implicit factory: Factory): LociSet = ParsedLoci(lociStr)
  def lociSet(lociStr: String)(implicit factory: Factory): LociSet = lociStr
  def lociSet(parsedLoci: ParsedLoci): LociSet = parsedLoci
}
