package models.graphql

import models.{ApplicationContext, Credentials}
import services.ServiceRegistry

/**
  * @author im.
  */
case class GraphQLContext(
                           app: ApplicationContext,
                           services: ServiceRegistry,
                           creds: Credentials
                         )
