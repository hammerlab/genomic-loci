package org.hammerlab.genomics.loci.parsing

import org.hammerlab.genomics.loci.set.TestLociSet
import org.hammerlab.magic.test.spark.SparkSuite

class ParsedLociSuite extends SparkSuite {

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    TestLociSet(
      ParsedLoci.fromArgs(
        lociStrOpt = None,
        lociFileOpt = Some("src/test/resources/truth.chr20.vcf"),
        sc.hadoopConfiguration
      ).get
    ).count should be(743606)
  }
}
