/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2003-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */



package scala.collection
package immutable

import generic._
import mutable.{ Builder, SetBuilder }

/** $factoryInfo
 *  @define Coll immutable.TreeSet
 *  @define coll immutable tree set
 */
object TreeSet extends ImmutableSortedSetFactory[TreeSet] {
  implicit def implicitBuilder[A](implicit ordering: Ordering[A]): Builder[A, TreeSet[A]] = newBuilder[A](ordering)
  override def newBuilder[A](implicit ordering: Ordering[A]): Builder[A, TreeSet[A]] =
    new SetBuilder(empty[A](ordering))

  /** The empty set of this type
   */
  def empty[A](implicit ordering: Ordering[A]) = new TreeSet[A]
}

/** This class implements immutable sets using a tree.
 *
 *  @tparam A         the type of the elements contained in this tree set
 *  @param ordering   the implicit ordering used to compare objects of type `A`
 *
 *  @author  Martin Odersky
 *  @version 2.0, 02/01/2007
 *  @since   1
 *  @see [[http://docs.scala-lang.org/overviews/collections/concrete-immutable-collection-classes.html#redblack_trees "Scala's Collection Library overview"]]
 *  section on `Red-Black Trees` for more information.
 *
 *  @define Coll immutable.TreeSet
 *  @define coll immutable tree set
 *  @define orderDependent
 *  @define orderDependentFold
 *  @define mayNotTerminateInf
 *  @define willNotTerminateInf
 */
@SerialVersionUID(-5685982407650748405L)
class TreeSet[A] private (tree: RedBlack.Tree[A, Unit])(implicit val ordering: Ordering[A])
  extends SortedSet[A] with SortedSetLike[A, TreeSet[A]] with Serializable {

  import immutable.{RedBlack => RB}

  override def stringPrefix = "TreeSet"

  override def size = RB.count(tree)

  override def head = RB.smallest(tree).key
  override def headOption = if (RB.isEmpty(tree)) None else Some(head)
  override def last = RB.greatest(tree).key
  override def lastOption = if (RB.isEmpty(tree)) None else Some(last)

  override def tail = new TreeSet(RB.delete(tree, firstKey))
  override def init = new TreeSet(RB.delete(tree, lastKey))

  override def drop(n: Int) = {
    if (n <= 0) this
    else if (n >= size) empty
    else from(tree.nth(n).key)
  }

  override def take(n: Int) = {
    if (n <= 0) empty
    else if (n >= size) this
    else until(tree.nth(n).key)
  }

  override def slice(from: Int, until: Int) = {
    if (until <= from) empty
    else if (from <= 0) take(until)
    else if (until >= size) drop(from)
    else range(tree.nth(from).key, tree.nth(until).key)
  }

  override def dropRight(n: Int) = take(size - n)
  override def takeRight(n: Int) = drop(size - n)
  override def splitAt(n: Int) = (take(n), drop(n))

  private[this] def countWhile(p: A => Boolean): Int = {
    var result = 0
    val it = iterator
    while (it.hasNext && p(it.next)) result += 1
    result
  }
  override def dropWhile(p: A => Boolean) = drop(countWhile(p))
  override def takeWhile(p: A => Boolean) = take(countWhile(p))
  override def span(p: A => Boolean) = {
    val n = countWhile(p)
    (take(n), drop(n))
  }

  def isSmaller(x: A, y: A) = compare(x,y) < 0

  def this()(implicit ordering: Ordering[A]) = this(null)(ordering)

  private def newSet(t: RedBlack.Tree[A, Unit]) = new TreeSet[A](t)

  /** A factory to create empty sets of the same type of keys.
   */
  override def empty = TreeSet.empty

  /** Creates a new `TreeSet` with the entry added.
   *
   *  @param elem    a new element to add.
   *  @return        a new $coll containing `elem` and all the elements of this $coll.
   */
  def + (elem: A): TreeSet[A] = newSet(RB.update(tree, elem, ()))

  /** A new `TreeSet` with the entry added is returned,
   *  assuming that elem is <em>not</em> in the TreeSet.
   *
   *  @param elem    a new element to add.
   *  @return        a new $coll containing `elem` and all the elements of this $coll.
   */
  def insert(elem: A): TreeSet[A] = {
    assert(!RB.contains(tree, elem))
    newSet(RB.update(tree, elem, ()))
  }

  /** Creates a new `TreeSet` with the entry removed.
   *
   *  @param elem    a new element to add.
   *  @return        a new $coll containing all the elements of this $coll except `elem`.
   */
  def - (elem:A): TreeSet[A] =
    if (!RB.contains(tree, elem)) this
    else newSet(RB.delete(tree, elem))

  /** Checks if this set contains element `elem`.
   *
   *  @param  elem    the element to check for membership.
   *  @return true, iff `elem` is contained in this set.
   */
  def contains(elem: A): Boolean = RB.contains(tree, elem)

  /** Creates a new iterator over all elements contained in this
   *  object.
   *
   *  @return the new iterator
   */
  def iterator: Iterator[A] = RB.keyIterator(tree)

  override def foreach[U](f: A =>  U) = RB.foreachKey(tree, f)

  override def rangeImpl(from: Option[A], until: Option[A]): TreeSet[A] = {
    val ntree = RB.range(tree, from, until)
    newSet(ntree)
  }
  override def firstKey = head
  override def lastKey = last
}
