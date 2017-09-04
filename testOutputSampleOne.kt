@file:Suppress("unused")

package com.prestongarno.ktq

import com.prestongarno.ktq.ArgBuilder
import com.prestongarno.ktq.InitStub
import com.prestongarno.ktq.ListConfig
import com.prestongarno.ktq.ListConfigType
import com.prestongarno.ktq.ListInitStub
import com.prestongarno.ktq.ListStub
import com.prestongarno.ktq.QConfigStub
import com.prestongarno.ktq.QInput
import com.prestongarno.ktq.QSchemaType
import com.prestongarno.ktq.QSchemaType.QScalar
import com.prestongarno.ktq.QSchemaType.QScalarList
import com.prestongarno.ktq.QSchemaType.QType
import com.prestongarno.ktq.QSchemaType.QTypeList
import com.prestongarno.ktq.QTypeConfigStub
import com.prestongarno.ktq.Stub
import com.prestongarno.ktq.TypeArgBuilder
import com.prestongarno.ktq.TypeListArgBuilder

abstract class BaseAvatarArgs(args: ArgBuilder) : ArgBuilder by args

interface ActorFoo : QSchemaType {
  val avatar: QConfigStub<URLY, BaseAvatarArgs>

  val login: Stub<String>

  val url: Stub<URLY>
}

object OrganizationFoo : QSchemaType, SomeConflict, ActorFoo {
  val owner: InitStub<UserFoo> by QType.stub()

  val name: Stub<String> by QScalar.stub()

  val members: ListInitStub<UserFoo> by QTypeList.stub()

  override val avatar: QConfigStub<URLY, AvatarArgs> by QScalar.configStub { AvatarArgs(it) }

  override val login: Stub<String> by QScalar.stub()

  override val url: Stub<URLY> by QScalar.stub()

  override val foobar: Stub<String> by QScalar.stub()

  class AvatarArgs(args: ArgBuilder) : BaseAvatarArgs(args) {
    fun size(value: Int): AvatarArgs = apply { addArg("size", value) }

  }
}

interface SomeConflict : QSchemaType {
  val avatar: QConfigStub<URLY, BaseAvatarArgs>

  val foobar: Stub<String>
}

object URLY : QSchemaType {
  val value: Stub<String> by QScalar.stub()
}

object UserFoo : QSchemaType, ActorFoo {
  override val avatar: QConfigStub<URLY, AvatarArgs> by QScalar.configStub { AvatarArgs(it) }

  override val login: Stub<String> by QScalar.stub()

  override val url: Stub<URLY> by QScalar.stub()

  class AvatarArgs(args: ArgBuilder) : BaseAvatarArgs(args) {
    fun size(value: Int): AvatarArgs = apply { addArg("size", value) }

  }
}
