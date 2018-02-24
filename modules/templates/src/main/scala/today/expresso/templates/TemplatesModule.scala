package today.expresso.templates

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.google.inject.Provides
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.{Configuration, Environment}
import play.api.inject._
import today.expresso.templates.api.HtmlCompiler
import today.expresso.templates.impl.HtmlCompilerImpl

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class TemplatesModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration) = {
      Seq(bind[HtmlCompiler].to[HtmlCompilerImpl])
  }

  @Provides
  def provideCompiler(configuration: Configuration, actorSystem: ActorSystem, injector: Injector): HtmlCompiler = {
    val config = configuration.underlying.as[CompilerConfig]("compiler")
    implicit val ec: ExecutionContext = actorSystem.dispatchers.lookup(config.dispatcher)
    new HtmlCompilerImpl(config.directory, injector)
  }
}
