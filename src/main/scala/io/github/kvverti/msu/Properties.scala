package io.github.kvverti.msu

import com.google.common.collect.ImmutableMap

import io.github.kvverti.msu.JavaStreams._

import it.unimi.dsi.fastutil.ints.{IntList, IntArrayList}

import java.util.Optional

import net.minecraft.state.PropertyContainer
import net.minecraft.state.property.Property

/**
 * Implicit utilities for working with properties and property containers.
 */
object Properties {


  implicit class ObjPropertyWrapper[A <: Comparable[A]](val self: Property[A]) extends AnyVal {
    def getVal(name: String): Option[A] = self.getValue(name).toOption
  }

  implicit class IntPropertyWrapper(val self: Property[Integer]) extends AnyVal {
    def getVal(name: String): Option[Int] = self.getValue(name).toOption.map(Int.unbox)
  }

  implicit class BooleanPropertyWrapper(val self: Property[java.lang.Boolean]) extends AnyVal {
    def getVal(name: String): Option[Boolean] = self.getValue(name).toOption.map(Boolean.unbox)
  }

  // this isn't actually the real type of the map either, but it's close enough
  // to be useful
  import scala.language.existentials
  type Entries = ImmutableMap[Property[A], A] forSome { type A <: Comparable[A] }

  implicit class PropertyContainerFixer[C <: PropertyContainer[C]](val self: C) extends AnyVal {
    def entries: Entries = self.getEntries.asInstanceOf[Entries]
    def getVal(prop: Property[Integer]): Int = Int.unbox(self.get(prop))
    def getVal(prop: Property[java.lang.Boolean]): Boolean = Boolean.unbox(self.get(prop))
    def getVal[A <: Comparable[A]](prop: Property[A]): A = self.get(prop)
    def withVal(prop: Property[Integer], value: Int): C = self.`with`(prop, Int.box(value))
    def withVal(prop: Property[java.lang.Boolean], value: Boolean): C = self.`with`(prop, Boolean.box(value))
    def withVal[A <: Comparable[A]](prop: Property[A], value: A): C = self.`with`(prop, value)
  }
}
