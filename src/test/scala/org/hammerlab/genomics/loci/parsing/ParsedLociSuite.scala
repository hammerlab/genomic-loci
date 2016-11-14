package org.hammerlab.genomics.loci.parsing

import org.hammerlab.genomics.loci.set.test.TestLociSet
import org.hammerlab.spark.test.suite.SparkSuite

class ParsedLociSuite extends SparkSuite {

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    val loci =
      TestLociSet(
        ParsedLoci.fromArgs(
          lociStrOpt = None,
          lociFileOpt = Some("src/test/resources/truth.chr20.vcf"),
          sc.hadoopConfiguration
        ).get
      )

    val ranges = loci.contigs(0).ranges
    println(s"${ranges.size} ${ranges.map(_.length).sum}")

    loci.count should be(743606)
  }
}
