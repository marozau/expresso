package clients

import java.io.{File, FileOutputStream}
import java.nio.file.Paths
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import org.reflections.Reflections
import play.api.inject.Injector
import play.api.{Configuration, Logger, Play}
import play.twirl.api.{BaseScalaTemplate, Html}
import play.twirl.compiler.GeneratedSource
import clients.Helper.CompilerHelper

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.io.Codec

/**
  * @author im.
  */
object Compiler {
  type HtmlTemplate = (Configuration) => Html

  //TODO: add context for complex solutions like specific word counting, statistics etc
  val header: String =
    """
        |@import _root_.services.TrackingService
        |@import _root_.play.api.Configuration
        |@this(implicit track: TrackingService)
        |@(implicit config: Configuration)
        """.stripMargin
  val headerLines = header.lines.size
}

@Singleton
class Compiler @Inject()(configuration: Configuration, actorSystem: ActorSystem, injector: Injector) {

  import Compiler._

  private val ec = actorSystem.dispatchers.lookup("compiler.blocking-dispatcher")

  private val config = configuration.get[Configuration]("compiler")
  private val dir = config.get[String]("workdir")
  private val sourceDir = Paths.get(dir, "source").toFile
  private val generatedDir =  Paths.get(dir, "generated-templates").toFile
  private val generatedClasses =  Paths.get(dir, "generated-classes").toFile

  Helper.deleteRecursively(sourceDir)
  sourceDir.mkdirs()
  Helper.deleteRecursively(generatedClasses)
  generatedClasses.mkdirs()
  Helper.deleteRecursively(generatedDir)
  generatedDir.mkdirs()

  val templateHelper = new TemplateHelper(sourceDir, generatedDir, generatedClasses)
  val reflectionsHelper = new ReflectionsHelper

  // https://github.com/playframework/twirl/tree/7a426ce67653188a7734a09391a609ee9dc3655f/compiler/src/test/resources
  // https://github.com/playframework/twirl/blob/master/compiler/src/test/scala/play/twirl/compiler/test/CompilerSpec.scala

  def compileBlocking(html: String, packageName: Option[String] = None, imports: Seq[String] = List.empty): HtmlTemplate = {
    val (templateName, template) = templateHelper.createTemplate(html)
    Logger.info(s"generate, template=$template")
    try {
      val className = templateHelper.getClassName(templateName)
      val viewsImports = packageName.map(reflectionsHelper.getImports).getOrElse(Seq.empty)
      val helper = new CompilerHelper(sourceDir, generatedDir, generatedClasses)
      val t = helper.compile[(HtmlTemplate)](template.getName, className, viewsImports ++ imports)
      t.guiceInject()
    } finally {
      templateHelper.deleteGenerated(templateName)
      templateHelper.deleteSource(templateName)
    }
  }

  def compile(html: String, packageName: Option[String] = None, imports: Seq[String] = List.empty): Future[HtmlTemplate] = {
    Future {
      compileBlocking(html, packageName, imports)
    }(ec)
  }
}

class ReflectionsHelper {

  private val root = "_root_."
  private val packages = Seq("views.html", "clients")

  private val imports: Seq[String] = packages.flatMap(new Reflections(_).getSubTypesOf(classOf[BaseScalaTemplate[_, _]]).asScala.map(root + _.getName.replace("$", "")).toSeq)
  private var cache: Map[String, Seq[String]] = Map.empty //TODO: replace by better cache implementation, caffeine for example

  def getImports(packageName: String): Seq[String] = {
    cache.get(packageName) match {
      case Some(v) => v
      case _ =>
        val i = imports.filter(_.startsWith(root + packageName))
        cache = cache + (packageName -> i)
        i
    }

  }
}

class TemplateHelper(sourceDir: File, generatedDir: File, generatedClasses: File) {
  final val prefix = "twirl_"
  final val postfix = ".scala.html"

  final val templatePostfix = ".template.scala"
  final val templateClassPostfix = ".class"
  final val html = "html"

  def createTemplate(html: String): (String, File) = {
    val file = File.createTempFile(prefix, postfix, sourceDir)
    val output = new FileOutputStream(file)
    output.write(html.getBytes)
    output.close()
    val templateName = file.getName.replace(postfix, "")
    (templateName, file)
  }

  def deleteGenerated(templateName: String) = {
    try {
      Paths.get(generatedClasses.getPath, html, templateName + templateClassPostfix).toFile.delete()
      Paths.get(generatedClasses.getPath, html, templateName + "$" + templateClassPostfix).toFile.delete()
      Paths.get(generatedDir.getPath, html, s"$templateName$templatePostfix").toFile.delete()
    } catch {
      case t: Throwable => Logger.error(s"failed to delete generated templateName=$templateName", t)
    }
  }

  def deleteSource(templateName: String) = {
    try {
      Paths.get(sourceDir.getPath, s"$templateName$postfix").toFile.delete()
    } catch {
      case t: Throwable => Logger.error(s"failed to delete source templateName=$templateName", t)
    }
  }

  def delete(dir: File, file: File): Unit = {
    new File(dir, file.getPath).delete()
  }

  def createGenerated() = {
    generatedDir.mkdirs()
    generatedClasses.mkdirs()
  }

  def getClassName(templateName: String) = s"$html.$templateName"
}

object Helper {

  def deleteRecursively(dir: File) {
    if (dir.isDirectory) {
      dir.listFiles().foreach(deleteRecursively)
    }
    dir.delete()
  }

  case class CompilationError(message: String, line: Int, column: Int) extends RuntimeException(message)

  class CompilerHelper(sourceDir: File, generatedDir: File, generatedClasses: File) {

    import java.net._

    import scala.collection.mutable
    import scala.reflect.internal.util.Position
    import scala.tools.nsc.reporters.ConsoleReporter
    import scala.tools.nsc.{Global, Settings}

    val twirlCompiler = play.twirl.compiler.TwirlCompiler

    //    val classloader = new URLClassLoader(Array(generatedClasses.toURI.toURL), Class.forName("play.twirl.compiler.TwirlCompiler").getClassLoader)
    val classloader = new URLClassLoader(Array(generatedClasses.toURI.toURL), java.lang.Thread.currentThread.getContextClassLoader)
    //    val classloader = java.lang.Thread.currentThread.getContextClassLoader

    // A list of the compile errors from the most recent compiler run
    val compileErrors = new mutable.ListBuffer[CompilationError]

    private val compiler = {

      def additionalClassPathEntry: Option[String] = Some(
        Class.forName("play.twirl.compiler.TwirlCompiler").getClassLoader.asInstanceOf[URLClassLoader].getURLs.map(url => new File(url.toURI)).mkString(":"))

      val settings = new Settings
      val scalaObjectSource = Class.forName("scala.Option").getProtectionDomain.getCodeSource

      // is null in Eclipse/OSGI but luckily we don't need it there
      if (scalaObjectSource != null) {
        val compilerPath = Class.forName("scala.tools.nsc.Interpreter").getProtectionDomain.getCodeSource.getLocation
        val injectPath = classOf[Inject].getProtectionDomain.getCodeSource.getLocation
        val libPath = scalaObjectSource.getLocation
        val pathList = List(compilerPath, libPath, injectPath)
        val origBootclasspath = settings.bootclasspath.value
        settings.bootclasspath.value = ((origBootclasspath :: pathList) ::: additionalClassPathEntry.toList) mkString File.pathSeparator

        lazy val urls = java.lang.Thread.currentThread.getContextClassLoader match {
          case cl: java.net.URLClassLoader => cl.getURLs.toList
          case _ => throw new RuntimeException("classloader is not a URLClassLoader")
        }
        lazy val classpath = urls map {
          _.toString
        }
        settings.classpath.value = classpath.distinct.mkString(java.io.File.pathSeparator)
        settings.outdir.value = generatedClasses.getAbsolutePath
      }

      val compiler = new Global(settings, new ConsoleReporter(settings) {
        override def printMessage(pos: Position, msg: String) = {
          compileErrors.append(CompilationError(msg, pos.line, pos.point))
        }
      })

      compiler
    }

    class CompiledTemplate[T](className: String) {

      private def getF(template: Any) = {
        template.getClass.getMethod("f").invoke(template).asInstanceOf[T]
      }

      def static: T = {
        getF(classloader.loadClass(className + "$").getDeclaredField("MODULE$").get(null))
      }

      def inject(constructorArgs: Any*): T = {
        classloader.loadClass(className).getConstructors match {
          case Array(single) => getF(single.newInstance(constructorArgs.asInstanceOf[Seq[AnyRef]]: _*))
          case other => throw new IllegalStateException(className + " does not declare exactly one constructor: " + other)
        }
      }

      def guiceInject(): T = {
        try {
          getF(Play.current.injector.instanceOf(classloader.loadClass(className)))
        } catch {
          case e: Exception => {
            throw new IllegalStateException("Problem instantiating class '" + className + "'", e)
          }
        }
      }
    }

    def compile[T](templateName: String, className: String, additionalImports: Seq[String] = Nil): CompiledTemplate[T] = {
      val templateFile = new File(sourceDir, templateName)
      val Some(generated) = twirlCompiler.compile(templateFile, sourceDir, generatedDir, "play.twirl.api.HtmlFormat", twirlCompiler.DefaultImports ++ additionalImports, Seq("@javax.inject.Inject()"))
      twirlCompiler.generatedFile(templateFile, Codec(scala.util.Properties.sourceEncoding), sourceDir, generatedDir, false)
      val mapper = GeneratedSource(generated)

      val run = new compiler.Run

      compileErrors.clear()

      run.compile(List(generated.getAbsolutePath))

      compileErrors.headOption.foreach {
        case CompilationError(msg, line, column) => {
          compileErrors.clear()
          throw CompilationError(msg, mapper.mapLine(line), mapper.mapPosition(column))
        }
      }

      new CompiledTemplate[T](className)
    }
  }

}