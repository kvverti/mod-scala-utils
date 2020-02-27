package io.github.kvverti.msu

import java.util
import java.util.stream.{Stream => ObjStream}
import java.util.stream.Collectors.joining

import scala.collection.TraversableOnce
import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.collection.mutable

object JavaStreams {

  implicit class StreamTraversable[A](val self: ObjStream[A])
      extends AnyVal
      with TraversableOnce[A] {

    override def isTraversableAgain: Boolean = false

    override def hasDefiniteSize: Boolean = false

    // there is no way to non-destructively test a java stream for emptyness
    override def isEmpty: Boolean = false

    override def exists(p: A => Boolean): Boolean = self.anyMatch(p(_))

    override def forall(p: A => Boolean): Boolean = self.allMatch(p(_))

    override def find(p: A => Boolean): Option[A] = self.filter(p(_)).findFirst.toOption

    override def seq: TraversableOnce[A] = new StreamTraversable(self.sequential)

    override def toStream: Stream[A] = {
      val itr = self.iterator
      def itr2stream: Stream[A] =
        if (itr.hasNext) itr.next #:: itr2stream else Stream.empty
      itr2stream
    }

    override def toIterator: Iterator[A] = self.iterator.asScala

    override def toTraversable: Traversable[A] = toVector

    override def copyToArray[B >: A](xs: Array[B], start: Int, len: Int): Unit = {
      val arr = self.limit(len).toArray
      System.arraycopy(arr, 0, xs, start, Array(len, xs.length, arr.length).min)
    }

    override def foreach[U](f: A => U): Unit = self.forEach(f(_))

    // overrides where java stream defines equivalents itself

    override def count(p: A => Boolean): Int = {
      val c = self.filter(p(_)).count
      if (c > Int.MaxValue) Int.MaxValue else c.toInt
    }

    override def size: Int = {
      val c = self.count
      if (c > Int.MaxValue) Int.MaxValue else c.toInt
    }

    def filter(p: A => Boolean): StreamTraversable[A] = new StreamTraversable(self.filter(p(_)))

    def withFilter(p: A => Boolean): StreamTraversable[A] = filter(p)

    def map[B](f: A => B): StreamTraversable[B] = new StreamTraversable(self.map(f(_)))

    override def fold[A1 >: A](z: A1)(op: (A1, A1) => A1): A1 = self.ofType[A1].reduce(z, op(_, _))

    override def reduceOption[A1 >: A](op: (A1, A1) => A1): Option[A1] =
      self.ofType[A1].reduce(op(_, _)).toOption

    override def reduce[A1 >: A](op: (A1, A1) => A1): A1 =
      self.ofType[A1].reduce(op(_, _)).orElseThrow(() => new UnsupportedOperationException())

    override def aggregate[B](z: => B)(seqop: (B, A) => B, combop: (B, B) => B): B =
      self.reduce(z, seqop(_, _), combop(_, _))

    override def mkString(start: String, sep: String, end: String): String =
      self.map[String](_.toString).collect(joining(start, sep, end))

    override def max[B >: A](implicit cmp: Ordering[B]): A =
      self.max(cmp).orElseThrow(() => new UnsupportedOperationException())

    override def maxBy[B](f: A => B)(implicit cmp: Ordering[B]): A =
      self.max((a, b) => cmp.compare(f(a), f(b))).orElseThrow(() => new UnsupportedOperationException())

    override def min[B >: A](implicit cmp: Ordering[B]): A =
      self.min(cmp).orElseThrow(() => new UnsupportedOperationException())

    override def minBy[B](f: A => B)(implicit cmp: Ordering[B]): A =
      self.min((a, b) => cmp.compare(f(a), f(b))).orElseThrow(() => new UnsupportedOperationException())

    override def product[B >: A](implicit num: Numeric[B]): B =
      self.ofType[B].reduce(num.one, num.times(_, _))

    override def sum[B >: A](implicit num: Numeric[B]): B =
      self.ofType[B].reduce(num.zero, num.plus(_, _))

    override def toVector: Vector[A] =
      self.collect[mutable.ArrayBuffer[A]](() => new mutable.ArrayBuffer(), _ += _, _ ++= _).toVector

    override def toIndexedSeq: immutable.IndexedSeq[A] = toVector

    // potentially useful methods

    // ObjStream uses its type parameter T in a contravariant position
    // as a parameter to ObjStream#reduce(id: T, ...). However, this is
    // a result of the Java language's lack of lower bounded type variables,
    // so it is safe to do the following unchecked cast.
    def ofType[A1 >: A]: ObjStream[A1] = self.asInstanceOf[ObjStream[A1]]
  }

  implicit class OptionalBridge[A](val self: util.Optional[A]) extends AnyVal {
    def withFilter(p: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, p)

    def foreach(f: A => Unit): Unit = self.ifPresent(f(_))

    def ofType[A1 >: A]: util.Optional[A1] = self.asInstanceOf[util.Optional[A1]]

    def toOption: Option[A] = if (self.isPresent) Some(self.get) else None
  }

  class OptionalBridgeWithFilter[A](self: util.Optional[A], p: A => Boolean) {
    def flatMap[B](f: A => util.Optional[B]): util.Optional[B] =
      self.filter(p(_)).flatMap(f(_))

    def map[B](f: A => B): util.Optional[B] = self.filter(p(_)).map(f(_))

    def withFilter(q: A => Boolean): OptionalBridgeWithFilter[A] =
      new OptionalBridgeWithFilter(self, a => p(a) && q(a))

    def foreach(f: A => Unit): Unit = self.filter(p(_)).ifPresent(f(_))
  }
}
