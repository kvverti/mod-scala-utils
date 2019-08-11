package io.github.kvverti.msu

import net.minecraft.block.{Block, BlockState}
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.BlockView

import scala.reflect.ClassTag

/**
 * Implicit extensions for accessing Blocks and BlockEntities
 * in a type safe manner.
 */
object WorldAccess {

  implicit class RichBlockState(val self: BlockState) extends AnyVal {

    /**
     * Get a block of a certain type, or None if the block is not of that type.
     * Useful in for comprehensions like so.
     * {{{
     *  for {
     *    pos <- neighbors
     *    state = world.getBlockState(pos)
     *    block <- state.typedBlock[MyBlock]
     *  } yield block.myMethod(state, world, pos)
     * }}}
     */
    def typedBlock[B <: Block: ClassTag]: Option[B] = {
      self.getBlock match {
        case b: B => Some(b)
        case _ => None
      }
    }

    /**
     * Apply the given partial function to this state's block, and return
     * the result. Useful for applying a single operation to a blockstate.
     * {{{
     *  state.collectBlock {
     *    case b: MyBlock => b.myMethod(state, world, pos)
     *  }
     * }}}
     */
    def collectBlock[A](pf: PartialFunction[Block, A]): Option[A] =
      Option(self.getBlock) collect pf
  }

  implicit class RichWorld(val self: BlockView) extends AnyVal {

    /**
     * Get a block entity of a certain type, or None if the block is not of
     * that type. Useful in for comprehensions like so.
     * {{{
     *  for {
     *    pos <- neighbors
     *    be <- world.typedBlockEntity[MyBlockEntity](pos)
     *    state = world.getBlockState(pos)
     *  } yield be.myMethod(state, world, pos)
     * }}}
     */
    def typedBlockEntity[B <: BlockEntity: ClassTag](pos: BlockPos): Option[B] = {
      self.getBlockEntity(pos) match {
        case b: B => Some(b)
        case _ => None
      }
    }

    /**
     * Apply the given partial function to the block entity at the given
     * position, and return the result. Useful for applying a single operation
     * to a block entity.
     * {{{
     *  world.collectBlockEntity(pos) {
     *    case b: MyBlockEntity => b.myMethod(state, world, pos)
     *  }
     * }}}
     */
    def collectBlockEntity[A](pos: BlockPos)(pf: PartialFunction[BlockEntity, A]): Option[A] =
      Option(self.getBlockEntity(pos)) collect pf
  }

  implicit class RichBlockPos(val self: BlockPos) extends AnyVal {

    /**
     * Return the six neighbor positions of this position.
     */
    def neighbors: Seq[BlockPos] = Direction.values.map(self.offset)
  }
}
