// PersonAndIdentityValues 定义共享内核中的通用值对象。

package com.typesafe.travel.shared.kernel

final case class PersonName(value: String)
object PersonName:
  export PersonAndIdentityValuesSupport.{createPersonName as create, unsafePersonName as unsafe}

final case class EmailAddress(value: String)
object EmailAddress:
  export PersonAndIdentityValuesSupport.{createEmailAddress as create, unsafeEmailAddress as unsafe}

final case class DocumentNumber(value: String)
object DocumentNumber:
  export PersonAndIdentityValuesSupport.{createDocumentNumber as create, unsafeDocumentNumber as unsafe}

final case class CountryCode(value: String)
object CountryCode:
  export PersonAndIdentityValuesSupport.{createCountryCode as create, unsafeCountryCode as unsafe}

final case class ContactNumber(value: String)
object ContactNumber:
  export PersonAndIdentityValuesSupport.{createContactNumber as create, unsafeContactNumber as unsafe}

final case class LoyaltyProgramName(value: String)
object LoyaltyProgramName:
  export PersonAndIdentityValuesSupport.{createLoyaltyProgramName as create, unsafeLoyaltyProgramName as unsafe}

final case class AvatarUrl(value: String)
object AvatarUrl:
  export PersonAndIdentityValuesSupport.{createAvatarUrl as create, unsafeAvatarUrl as unsafe}
