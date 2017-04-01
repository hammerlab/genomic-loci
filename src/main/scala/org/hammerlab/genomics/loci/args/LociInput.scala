package org.hammerlab.genomics.loci.args

import java.nio.file.Path

trait LociInput {
  def lociStrOpt: Option[String]
  def lociFileOpt: Option[Path]
}
