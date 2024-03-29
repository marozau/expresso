package controllers

import java.util.jar.Manifest
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import modules.DefaultEnv
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * @author im.
  */
@Singleton
class ManifestController @Inject()(
                                    cc: ControllerComponents,
                                    env: Environment[DefaultEnv],
                                    silhouette: Silhouette[DefaultEnv]) extends AbstractController(cc) {

  import scala.collection.JavaConverters._

  private val manifest = {
    val inputStream = Thread.currentThread().getContextClassLoader.getResourceAsStream("META-INF/MANIFEST.MF")
    new Manifest(inputStream)
  }
  Logger.info("Manifest:\n" + manifest.getMainAttributes.asScala.mkString(","))

  def get() = Action {
    Ok(Json.toJson(manifest.getMainAttributes.asScala.map { case (k, v) => (k.toString, v.toString) }))
  }
}
