// SiteAdminManagerPlannerSupport 负责operations相关实现。

package com.typesafe.travel.operations.domain

import cats.effect.IO

def validateRegisterSiteAdmin(input: RegisterSiteAdminPlannerRequest): IO[Unit] =
  IO {
    require(input.email.trim.nonEmpty, "email is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
    require(input.password.nonEmpty, "password is required")
  }

def validateUpdateSiteAdminProfile(input: UpdateSiteAdminManagerProfilePlannerRequest): IO[Unit] =
  IO {
    require(input.managerId.trim.nonEmpty, "managerId is required")
    require(input.displayName.trim.nonEmpty, "displayName is required")
  }
