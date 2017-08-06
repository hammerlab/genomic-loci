package org.hammerlab.genomics.loci.parsing

import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.ClearContigNames
import org.hammerlab.genomics.reference.test.LociConversions.intToLocus
import org.hammerlab.test.Suite
import org.hammerlab.test.resources.File

class ParsedLociSuite
  extends Suite
    with ClearContigNames
    with LociSetUtil {

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    val loci =
      lociSet(
        ParsedLoci(
          lociStrOpt = None,
          lociFileOpt = Some(File("truth.chr20.vcf").path)
        ).get
      )

    loci.count should ===(743606)
  }
}
