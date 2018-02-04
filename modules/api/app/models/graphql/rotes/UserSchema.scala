package models.graphql.rotes

import models.graphql.GraphQLContext
import models.graphql.dto.UserDtoSchema.{ID, UserDtoType}
import sangria.schema.{Field, ObjectType, OptionType, Schema, fields}

/**
  * @author im.
  */
object UserSchema {

  val Query = ObjectType[GraphQLContext, Unit](
    "Query", fields[GraphQLContext, Unit](
      Field("me", OptionType(UserDtoType),
        resolve = ctx => ctx.ctx.services.userService.getById(ctx.ctx.creds.user.id)(ctx.ctx.creds))
    ))

  val schema = Schema(Query)
}
