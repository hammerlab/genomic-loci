package org.hammerlab.genomics.loci.parsing

import org.apache.hadoop.conf.Configuration
import org.hammerlab.genomics.loci.set.test.TestLociSet
import org.hammerlab.test.Suite

class ParsedLociSuite extends Suite {

  val conf = new Configuration

  // Loci-from-VCF sanity check.
  test("vcf loading") {
    val loci =
      TestLociSet(
        ParsedLoci.fromArgs(
          lociStrOpt = None,
          lociFileOpt = Some("src/test/resources/truth.chr20.vcf"),
          conf
        ).get
      )

    val ranges = loci.contigs(0).ranges
    println(s"${ranges.size} ${ranges.map(_.length).sum}")

    loci.count === 743606
  }
}
