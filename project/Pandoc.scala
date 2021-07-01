
object Pandoc {
  def commandLine(
    pages: List[String],
    output: String,
    metadata: List[String] = Nil,
    template: Option[String] = None,
    filters: List[String] = Nil,
    filenameStem: String = "essential-scala",
    pagesDir: String = "target/pages",
    srcDir: String = "src",
    distDir: String = "dist",
    tocDepth: Int = 3,
    extraArgs: List[String] = Nil,
  ): String = {
    val parts = List(
      List(
        "pandoc",
        s"--output=$output",
        "--from=markdown+grid_tables+multiline_tables+fenced_code_blocks+fenced_code_attributes+yaml_metadata_block+implicit_figures+header_attributes+definition_lists+link_attributes",
      ),
      template.map(name => s"--template=$name").toList,
      filters.map(name => s"--filter=$name"),
      List(
        "--top-level-division=chapter",
        "--number-sections",
        "--table-of-contents",
        "--highlight-style tango",
        "--standalone",
        "--self-contained",
        s"--toc-depth=${tocDepth}",
      ),
      extraArgs,
      metadata.map(file => s"--metadata-file=$file"),
      pages,
    ).flatten

    println("====================\n" + parts.mkString(" ") + "\n====================")

    parts.mkString(" ")
  }
}