package org.hammerlab.genomics.loci.parsing

import htsjdk.variant.vcf.VCFFileReader
import org.hammerlab.genomics.loci.VariantContext
import org.hammerlab.genomics.loci.args.LociArgs
import org.hammerlab.genomics.reference.ContigName.Factory
import org.hammerlab.paths.Path

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

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
 *   - [[All]]: sentinel value representing all loci on all contigs.
 *   - [[LociRanges]]: a sequence of [[LociRange]]s denoting (possibly open-ended) genomic-intervals.
 *
 * Examples:
 *
 *   - chr1,chrY
 *   - chr2:0-100
 *   - chr1:123-124,chr5:456-457
 *   - all
 */
sealed trait ParsedLoci extends Any

object ParsedLoci {
  def apply(lociStrs: String)(implicit factory: Factory): ParsedLoci = apply(Iterator(lociStrs))

  def apply(lines: Iterator[String])(implicit factory: Factory): ParsedLoci = {
    val lociRanges = ArrayBuffer[LociRange]()
    for {
      lociStrs ← lines
      lociStr ← lociStrs.replaceAll("\\s", "").split(",")
      lociRange ← ParsedLociRange(lociStr)
    } {
      lociRange match {
        case AllRange ⇒ return All
        case lociRange: LociRange ⇒
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
  def apply(args: LociArgs): Option[ParsedLoci] =
    apply(
      args.lociStrOpt,
      args.lociFileOpt
    )

  def apply(lociStrOpt: Option[String],
            lociFileOpt: Option[Path])(implicit factory: Factory): Option[ParsedLoci] =
    (lociStrOpt, lociFileOpt) match {
      case (Some(lociStr), _) => Some(ParsedLoci(lociStr))
      case (_, Some(lociPath)) => Some(loadFromPath(lociPath))
      case _ =>
        None
    }

  /**
   * Parse loci from the specified file.
   *
   * @param path path to file containing loci. If it ends in '.vcf' then it is read as a VCF and the variant sites
   *                 are the loci. If it ends in '.loci' or '.txt' then it should be a file containing loci as
   *                 "chrX:5-10,chr12-10-20", etc. Whitespace is ignored.
   * @return parsed loci
   */
  private def loadFromPath(path: Path)(implicit factory: Factory): ParsedLoci =
    path.extension match {
      case "vcf" ⇒ LociRanges.fromVCF(path)
      case "loci" | "txt" ⇒ ParsedLoci(path.lines)
      case _ ⇒
        throw new IllegalArgumentException(
          s"Couldn't guess format for file: $path. Expected file extensions: '.loci' or '.txt' for loci string format; '.vcf' for VCFs."
        )
    }
}

/**
 * Special [[ParsedLoci]] value representing all genomic loci.
 */
case object All extends ParsedLoci

case class LociRanges(ranges: Iterable[LociRange]) extends AnyVal with ParsedLoci

object LociRanges {
  def apply(range: LociRange): LociRanges = apply(Iterable(range))

  def fromVCF(path: Path): LociRanges =
    apply(
      // VCF-reading currently only works for local files, requires "file://" scheme to not be present.
      // TODO: use hadoop-bam to load VCF from local filesystem or HDFS.
      new VCFFileReader(path.toFile, false)
        .map {
          case VariantContext(contigName, start, end) =>
            LociRange(contigName, start, end)
        }
    )
}
