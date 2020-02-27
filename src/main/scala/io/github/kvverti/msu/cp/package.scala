package io.github.kvverti.msu

import net.minecraft.{util => mcu}

/**
 * Companion objects for common Minecraft data classes.
 */
package object cp {

  type Identifier = mcu.Identifier

  object Identifier {

    def apply(namespace: String, path: String): Identifier = new Identifier(namespace, path)

    def unapply(id: Identifier): Option[(String, String)] =
      if (id == null) None else Some((id.getNamespace, id.getPath))
  }

  type BlockPos = mcu.math.BlockPos

  object BlockPos {

    val Origin: BlockPos = mcu.math.BlockPos.ORIGIN

    def apply(x: Int, y: Int, z: Int): BlockPos = new BlockPos(x, y, z)

    def unapply(pos: BlockPos): Option[(Int, Int, Int)] =
      if (pos == null) None else Some((pos.getX, pos.getY, pos.getZ))
  }

  type Vec3i = mcu.math.Vec3i

  object Vec3i {

    val Zero: Vec3i = mcu.math.Vec3i.ZERO

    def apply(x: Int, y: Int, z: Int): Vec3i = new Vec3i(x, y, z)

    def unapply(pos: Vec3i): Option[(Int, Int, Int)] =
      if (pos == null) None else Some((pos.getX, pos.getY, pos.getZ))
  }

  type Vec3d = mcu.math.Vec3d

  object Vec3d {

    val Zero: Vec3d = mcu.math.Vec3d.ZERO

    def apply(x: Double, y: Double, z: Double): Vec3d = new Vec3d(x, y, z)

    def unapply(pos: Vec3d): Option[(Double, Double, Double)] =
      if (pos == null) None else Some((pos.x, pos.y, pos.z))
  }

  type Position = mcu.math.Position

  object Position {

    def unapply(pos: Position): Option[(Double, Double, Double)] =
      if (pos == null) None else Some((pos.getX, pos.getY, pos.getZ))
  }
}
