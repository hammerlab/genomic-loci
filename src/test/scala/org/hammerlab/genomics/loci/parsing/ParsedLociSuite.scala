package org.hammerlab.genomics.loci.parsing

import org.apache.hadoop.conf.Configuration
import org.hammerlab.genomics.loci.set.test.LociSetUtil
import org.hammerlab.genomics.reference.test.LocusUtil
import org.hammerlab.test.Suite

class ParsedLociSuite
  extends Suite
    with LocusUtil
    with LociSetUtil {

  val conf = new Configuration

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    val loci =
      lociSet(
        ParsedLoci.fromArgs(
          lociStrOpt = None,
          lociFileOpt = Some("src/test/resources/truth.chr20.vcf"),
          conf
        ).get
      )

    loci.count should ===(743606)
  }
}
