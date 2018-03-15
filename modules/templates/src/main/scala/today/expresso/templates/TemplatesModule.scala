package today.expresso.templates

import javax.inject.{Inject, Provider, Singleton}

import akka.actor.ActorSystem
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.{Configuration, Environment}
import play.api.inject._
import today.expresso.templates.api.HtmlCompiler
import today.expresso.templates.impl.{CompilerCache, CompilerCacheImpl, HtmlCompilerImpl}

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class TemplatesModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration) = {
      Seq(
        bind[HtmlCompiler].toProvider[HtmlCompilerProvider],
        bind[CompilerCache].to(classOf[CompilerCacheImpl])
      )
  }
}

class HtmlCompilerProvider @Inject()(configuration: Configuration, actorSystem: ActorSystem, injector: Injector) extends Provider[HtmlCompiler] {
  override def get() = {
    val config = configuration.underlying.as[CompilerConfig]("compiler")
    implicit val ec: ExecutionContext = actorSystem.dispatchers.lookup(config.dispatcher)
    new HtmlCompilerImpl(config.directory, injector)
  }
}