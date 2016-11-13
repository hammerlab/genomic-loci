package org.hammerlab.genomics.loci.parsing

import java.io.File
import scala.collection.JavaConversions._

import htsjdk.variant.vcf.VCFFileReader
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
 * Representation of genomic-loci ranges that may be used to instantiate a [[org.hammerlab.genomics.loci.set.LociSet]].
 *
 * Constituent ranges can be open-ended, so a [[ParsedLoci]] is typically a short-lived intermediate representation
 * between [[String]] representations of ranges (possibly originating from cmdline-flags or a file) and a
 * [[org.hammerlab.genomics.loci.set.LociSet]] whose open-ended ranges have been "resolved" using contig-length
 * information found in a BAM header.
 *
 * The two implementations are:
 *
 *   - [[All]]: all loci on all contigs.
 *   - [[LociRanges]]: a sequence of [[LociRange]]s denoting (possibly open-ended) genomic-intervals.
 */
sealed trait ParsedLoci extends Any

object ParsedLoci {
  def apply(lociStrs: String): ParsedLoci = apply(Iterator(lociStrs))

  def apply(lines: Iterator[String]): ParsedLoci = {
    val lociRanges = ArrayBuffer[LociRange]()
    for {
      lociStrs <- lines
      lociStr <- lociStrs.replaceAll("\\s", "").split(",")
      lociRange <- ParsedLociRange(lociStr)
    } {
      lociRange match {
        case AllRange => return All
        case lociRange: LociRange =>
          lociRanges += lociRange
      }
    }
    LociRanges(lociRanges)
  }

  /**
   * Parse string representations of loci ranges, either from one string (lociOpt) or a file with one range per line
   * (lociFileOpt), and return a [[ParsedLoci]] encapsulating the result. The latter can then be converted into a
   * [[org.hammerlab.genomics.loci.set.LociSet]] when contig-lengths are available / have been parsed from read-sets.
   */
  def fromArgs(lociStrOpt: Option[String],
               lociFileOpt: Option[String],
               hadoopConfiguration: Configuration): Option[ParsedLoci] =
    (lociStrOpt, lociFileOpt) match {
      case (Some(lociStr), _) => Some(ParsedLoci(lociStr))
      case (_, Some(lociFile)) => Some(loadFromFile(lociFile, hadoopConfiguration))
      case _ =>
        None
    }

  /**
   * Parse loci from the specified file.
   *
   * @param lociFile path to file containing loci. If it ends in '.vcf' then it is read as a VCF and the variant sites
   *                 are the loci. If it ends in '.loci' or '.txt' then it should be a file containing loci as
   *                 "chrX:5-10,chr12-10-20", etc. Whitespace is ignored.
   * @return parsed loci
   */
  private def loadFromFile(lociFile: String, hadoopConfiguration: Configuration): ParsedLoci =
    if (lociFile.endsWith(".vcf")) {
      LociRanges.fromVCF(lociFile)
    } else if (lociFile.endsWith(".loci") || lociFile.endsWith(".txt")) {
      val path = new Path(lociFile)
      val filesystem = path.getFileSystem(hadoopConfiguration)
      val is = filesystem.open(path)
      val lines = Source.fromInputStream(is).getLines()
      ParsedLoci(lines)
    } else
      throw new IllegalArgumentException(
        s"Couldn't guess format for file: $lociFile. Expected file extensions: '.loci' or '.txt' for loci string format; '.vcf' for VCFs."
      )
}

/**
 * Special [[ParsedLoci]] value representing all genomic loci.
 */
case object All extends ParsedLoci

case class LociRanges(ranges: Iterable[LociRange]) extends AnyVal with ParsedLoci

object LociRanges {
  def apply(range: LociRange): LociRanges = apply(Iterable(range))

  def fromVCF(lociFile: String): LociRanges =
    apply(
      // VCF-reading currently only works for local files, requires "file://" scheme to not be present.
      // TODO: use hadoop-bam to load VCF from local filesystem or HDFS.
      new VCFFileReader(new File(lociFile), false)
        .map(variant =>
          LociRange(variant.getContig, variant.getStart, variant.getEnd)
        )
    )
}
