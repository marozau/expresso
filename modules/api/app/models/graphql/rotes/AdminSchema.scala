package models.graphql.rotes

import models.graphql.GraphQLContext
import models.graphql.dto.UserDtoSchema.{ID, UserDtoType}
import sangria.schema.{Field, ObjectType, OptionType, Schema, fields}

/**
  * @author im.
  */
object AdminSchema {

  val Query = ObjectType[GraphQLContext, Unit](
    "Query", fields[GraphQLContext, Unit](
      Field("user", OptionType(UserDtoType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.services.userService.getById(ctx.arg(ID))(ctx.ctx.creds))
    ))

  val schema = Schema(Query)
}
