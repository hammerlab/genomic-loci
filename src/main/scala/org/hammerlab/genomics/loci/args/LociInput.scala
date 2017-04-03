package org.hammerlab.genomics.loci.args

import org.hammerlab.paths.Path

trait LociInput {
  def lociStrOpt: Option[String]
  def lociFileOpt: Option[Path]
}
