package io.github.kvverti.msu

import net.minecraft.block.{Block, BlockState}

import scala.reflect.ClassTag

object BlockStates {

  implicit class RichBlockState(val self: BlockState) extends AnyVal {

    def typedBlock[B <: Block: ClassTag]: Option[B] = {
      self.getBlock match {
        case b: B => Some(b)
        case _ => None
      }
    }
  }
}
