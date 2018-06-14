package org.hammerlab.genomics.loci.parsing

import hammerlab.test.Suite
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.ClearContigNames
import org.hammerlab.genomics.reference.test.LociConversions.intToLocus
import org.hammerlab.test.resources.File

class ParsedLociSuite
  extends Suite
    with ClearContigNames
    with LociSetUtil {

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    val loci =
      lociSet(
        ParsedLoci.loadFromPath(
          File("truth.chr20.vcf")
        )
      )

    ==(loci.count, 743606)
  }
}
