import java.nio.file.{Files, Path, Paths}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}
import play.api.Configuration
import play.api.inject.guice.GuiceInjectorBuilder
import today.expresso.templates.api.HtmlCompiler
import today.expresso.templates.api.HtmlCompiler.HtmlTemplate
import today.expresso.templates.api.domain.Target
import today.expresso.templates.impl.{Helper, HtmlCompilerImpl}

import scala.reflect.internal.util.ScalaClassLoader

/**
  * @author im.
  */
class CompilerSpec extends WordSpec
  with WordSpecLike
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  java.lang.Thread.currentThread.setContextClassLoader(new ScalaClassLoader.URLClassLoader(Seq(), getClass.getClassLoader))

  val injector = new GuiceInjectorBuilder().build()
  val folderPath: Path = Paths.get("modules/templates/src/test/resources/")
  val tmpDir: Path = Files.createTempDirectory(folderPath, null)
  val compiler: HtmlCompiler = new HtmlCompilerImpl(tmpDir.toString, injector)(scala.concurrent.ExecutionContext.global)

  override protected def afterAll(): Unit = {
    Helper.deleteRecursively(tmpDir.toFile)
  }

  "Compiler" should {
    "create html from @_text dsl" in {
      val dsl =
        """
          |@_text("hello", true, true)
        """.stripMargin
      val template: HtmlTemplate = compiler.compile(dsl, Some("templates.compiler." + Target.DEV.toString.toLowerCase + ".html"))
      val body = template(Configuration.empty).body
      body.trim should be("<i><strong>hello</strong></i>")
    }

    "create html from @_image dsl" in {
      val dsl =
        """
          |@_image("image", Some("https://expresso.today"))
        """.stripMargin
      val template: HtmlTemplate = compiler.compile(dsl, Some("templates.compiler." + Target.DEV.toString.toLowerCase + ".html"))
      val body = template(Configuration.empty).body
      body.trim should startWith("<a href=\"https://expresso.today\"")
    }
  }

  "create html from @_href dsl" in {
    val dsl =
      """
        |@_href("hello", "https://expresso.today", true, true)
      """.stripMargin
    val template: HtmlTemplate = compiler.compile(dsl, Some("templates.compiler." + Target.DEV.toString.toLowerCase + ".html"))
    val body = template(Configuration.empty).body
    body.trim should startWith("<a href=\"https://expresso.today\"")
  }
}
