package amyc

import utils._
import ast._
import parsing._
import analyzer._
import parsing.ASTPrecedenceCorrector
import codegen._

import java.io.File

object Main extends MainHelpers {
  private def parseArgs(args: Array[String]): Context = {
    Context(new Reporter, args.toList)
  }

  def main(args: Array[String]): Unit = {
    val ctx = parseArgs(args)
    val pipeline =
      Lexer andThen
      Parser andThen
     	PreAnalyzer andThen
      ASTPrecedenceCorrector andThen
      //treePrinterN("Tree after correction")
      NameAnalyzer andThen
      TypeChecker andThen
      CodeGen andThen
      CodePrinter
    val operators = new File(s"library/Operators.scala")
    val files = operators :: ctx.files.map(new File(_))

    try {
      if (files.isEmpty) {
        ctx.reporter.fatal("No input files")
      }
      files.find(!_.exists()).foreach { f =>
        ctx.reporter.fatal(s"File not found: ${f.getName}")
      }
      pipeline.run(ctx)(files)
      ctx.reporter.terminateIfErrors()
    } catch {
      case AmycFatalError(_) =>
        sys.exit(1)
    }
  }
}

trait MainHelpers {
  import SymbolicTreeModule.{Program => SP}
  import NominalTreeModule.{Program => NP}

  def treePrinterS(title: String): Pipeline[(SP, SymbolTable), Unit] = {
    new Pipeline[(SP, SymbolTable), Unit] {
      def run(ctx: Context)(v: (SP, SymbolTable)) = {
        println(title)
        println(SymbolicPrinter(v._1)(true))
      }
    }
  }

  def treePrinterN(title: String): Pipeline[(NP,SymbolTable), Unit] = {
    new Pipeline[(NP, SymbolTable), Unit] {
      def run(ctx: Context)(v: (NP, SymbolTable)) = {
        println(title)
        println(NominalPrinter(v._1))
      }
    }
  }
}
