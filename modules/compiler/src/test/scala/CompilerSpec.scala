import java.nio.file.{Files, Path, Paths}

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}
import play.api.Configuration
import play.api.inject.guice.GuiceInjectorBuilder
import today.expresso.compiler.api.HtmlCompiler
import today.expresso.compiler.api.HtmlCompiler.HtmlTemplate
import today.expresso.compiler.api.domain.Target
import today.expresso.compiler.impl.{Helper, HtmlCompilerImpl}

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
  val folderPath: Path = Paths.get("modules/compiler/src/test/resources/")
  val tmpDir: Path = Files.createTempDirectory(folderPath, null)
  val compiler: HtmlCompiler = new HtmlCompilerImpl(tmpDir.toString, injector)(scala.concurrent.ExecutionContext.global)

  override protected def afterAll(): Unit = {
    Helper.deleteRecursively(tmpDir.toFile)
  }

  "Compiler" should {
    "create html from dsl" in {
      val dsl =
        """
          |@_text("hello", true, true)
        """.stripMargin
      val template: HtmlTemplate = compiler.compile(dsl, Some("templates.compiler." + Target.DEV.toString.toLowerCase + ".html"))
      val body = template(Configuration.empty).body
      body.trim should be("<i><strong>hello</strong></i>")
    }
  }
}
