package services.graphql

import javax.inject.{Inject, Singleton}

import models.graphql.{GraphQLContext, UserDtoSchema}
import models.{ApplicationContext, Credentials}
import play.api.libs.json.{JsObject, Json}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import services.ServiceRegistry
import sangria.marshalling.playJson._

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class GraphQLService @Inject() (registry: ServiceRegistry)(implicit ec: ExecutionContext) {

  def executeQuery(query: Document, operation: Option[String], variables: Option[JsObject])
                  (app: ApplicationContext, creds: Credentials, debug: Boolean) = {
    Executor.execute(
      schema = UserDtoSchema.UserDtoSchema,
      queryAst = query,
      userContext = GraphQLContext(app, registry, creds) ,
      operationName = operation,
      variables = variables.getOrElse(Json.obj()))
      .map(play.api.mvc.Results.Ok(_))
      .recover {
        case error: QueryAnalysisError => play.api.mvc.Results.BadRequest(error.resolveError)
        case error: ErrorWithResolver => play.api.mvc.Results.BadRequest(error.resolveError)
      }
  }
}
