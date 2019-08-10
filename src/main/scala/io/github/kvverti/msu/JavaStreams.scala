package io.github.kvverti.msu

import java.util
import java.util.function.{BiConsumer, BinaryOperator, Function, Supplier}
import java.util.stream.{Collector, Stream => ObjStream}

import scala.collection.mutable

object JavaStreams {

  implicit class ObjStreamBridge[A](val self: ObjStream[A]) extends AnyVal {
    def withFilter(f: A => Boolean): ObjStream[A] = self filter { f(_) }
    def foreach(f: A => Unit): Unit = self forEach { f(_) }
  }

  implicit class OptionalBridge[A](val self: util.Optional[A]) extends AnyVal {
    def withFilter(p: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, p)
    def foreach(f: A => Unit): Unit = self ifPresent { f(_) }
    def toOption: Option[A] = if (self.isPresent) Some(self.get) else None
  }

  class OptionalBridgeWithFilter[A](self: util.Optional[A], p: A => Boolean) {
    def flatMap[B, B1 <: B](f: A => util.Optional[B1]): util.Optional[B] =
      self filter { p(_) } flatMap { f(_) }
    def map[B](f: A => B): util.Optional[B] = self filter { p(_) } map { f(_) }
    def withFilter(q: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, a => p(a) && q(a))
    def foreach(f: A => Unit): Unit = self filter { p(_) } ifPresent { f(_) }
  }

  // Java Collectors are invariant and have overall bad typing

  def toScalaVector[A]: Collector[A, _, Vector[A]] = ScalaVectorCollector.asInstanceOf[Collector[A, _, Vector[A]]]

  def toScalaList[A]: Collector[A, _, List[A]] = ScalaListCollector.asInstanceOf[Collector[A, _, List[A]]]

  def toScalaSet[A]: Collector[A, _, Set[A]] = ScalaSetCollector.asInstanceOf[Collector[A, _, Set[A]]]

  def toScalaSeq[A]: Collector[A, _, Seq[A]] = ScalaSeqCollector.asInstanceOf[Collector[A, _, Seq[A]]]

  private abstract class ScalaCollector[C] extends Collector[Any, mutable.ArrayBuffer[Any], C] {
    override def characteristics: util.Set[Collector.Characteristics] = util.Collections.emptySet[Collector.Characteristics]
    override def supplier: Supplier[mutable.ArrayBuffer[Any]] = () => new mutable.ArrayBuffer()
    override def accumulator: BiConsumer[mutable.ArrayBuffer[Any], Any] = _ += _
    override def combiner: BinaryOperator[mutable.ArrayBuffer[Any]] = _ ++ _
  }

  private object ScalaVectorCollector extends ScalaCollector[Vector[Any]] {
    override def finisher: Function[mutable.ArrayBuffer[Any], Vector[Any]] = _.toVector
  }

  private object ScalaListCollector extends ScalaCollector[List[Any]] {
    override def finisher: Function[mutable.ArrayBuffer[Any], List[Any]] = _.toList
  }

  private object ScalaSetCollector extends ScalaCollector[Set[Any]] {
    override def finisher: Function[mutable.ArrayBuffer[Any], Set[Any]] = _.toSet
  }

  private object ScalaSeqCollector extends ScalaCollector[Seq[Any]] {
    override def finisher: Function[mutable.ArrayBuffer[Any], Seq[Any]] = _.toSeq
  }
}
