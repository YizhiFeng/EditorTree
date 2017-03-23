package editortrees;

import java.util.ArrayList;
import java.util.Stack;

// A height-balanced binary tree with rank that could be the basis for a text editor.

public class EditTree {

	private Node root;
	public final Node NULL_NODE = new NullNode(this);
	private int totalRotationCount;

	/**
	 * Construct an empty tree
	 */
	public EditTree() {
		this.root = this.NULL_NODE;
		this.totalRotationCount = 0;
	}

	/**
	 * Construct a single-node tree whose element is c
	 * 
	 * @param c
	 */
	public EditTree(char c) {
		this();
		this.root = new Node(c, this);
		this.totalRotationCount = 0;
	}

	/**
	 * Create an EditTree whose toString is s. This can be done in O(N) time,
	 * where N is the length of the tree (repeatedly calling insert() would be
	 * O(N log N), so you need to find a more efficient way to do this.
	 * 
	 * @param s
	 */
	public EditTree(String s) {
		// calling another constructor to create an empty tree.
		this();
		this.root = this.root.forConstructorUsingString(s);
	}

	/**
	 * Make this tree be a copy of e, with all new nodes, but the same shape and
	 * contents.
	 * 
	 * @param e
	 */
	public EditTree(EditTree e) {
		this.root = e.root.copy(this, this.NULL_NODE);
		this.totalRotationCount = e.totalRotationCount;
	}

	/**
	 * 
	 * @return the height of this tree
	 */
	public int height() {
		return this.root.height();
	}

	/**
	 * 
	 * returns the total number of rotations done in this tree since it was
	 * created. A double rotation counts as two.
	 *
	 * @return number of rotations since tree was created.
	 */
	public int totalRotationCount() {
		return this.totalRotationCount;
	}

	/**
	 * 
	 * increase the totalRotationCount by 1.
	 *
	 */
	public void increaseTRC() {
		this.totalRotationCount++;
	}

	/**
	 * return the string produced by an inorder traversal of this tree
	 */
	@Override
	public String toString() {
		return this.root.toString();

	}

	/**
	 * This one asks for more info from each node. You can write it like the
	 * arraylist-based toString() method from the BST assignment. However, the
	 * output isn't just the elements, but the elements, ranks, and balance
	 * codes. Former CSSE230 students recommended that this method, while making
	 * it harder to pass tests initially, saves them time later since it catches
	 * weird errors that occur when you don't update ranks and balance codes
	 * correctly. For the tree with node b and children a and c, it should
	 * return the string: [b1=, a0=, c0=] There are many more examples in the
	 * unit tests.
	 * 
	 * @return The string of elements, ranks, and balance codes, given in a
	 *         pre-order traversal of the tree.
	 */
	public String toDebugString() {
		ArrayList<String> ar = new ArrayList<String>();
		this.root.toDebugString(ar);
		return ar.toString();
	}

	/**
	 * 
	 * @param pos
	 *            position in the tree
	 * @return the character at that position
	 * @throws IndexOutOfBoundsException
	 */
	public char get(int pos) throws IndexOutOfBoundsException {
		if (pos < 0 || pos + 1 > this.size() || this.size() == 0)
			throw new IndexOutOfBoundsException();
		return this.root.get(pos);
	}

	/**
	 * 
	 * @param c
	 *            character to add to the end of this tree.
	 */
	public void add(char c) {
		// Notes:
		// 1. Please document chunks of code as you go. Why are you doing what
		// you are doing? Comments written after the code is finalized tend to
		// be useless, since they just say WHAT the code does, line by line,
		// rather than WHY the code was written like that. Six months from now,
		// it's the reasoning behind doing what you did that will be valuable to
		// you!
		// 2. Unit tests are cumulative, and many things are based on add(), so
		// make sure that you get this one correct.
		if (this.root.equals(this.NULL_NODE)) {
			this.root = new Node(c, this);
		} else {
			this.root = this.root.add(c);
		}
	}

	/**
	 * 
	 * @param c
	 *            character to add
	 * @param pos
	 *            character added in this inorder position
	 * @throws IndexOutOfBoundsException
	 *             id pos is negative or too large for this tree
	 */
	public void add(char c, int pos) throws IndexOutOfBoundsException {
		if (pos < 0 || pos > this.size())
			throw new IndexOutOfBoundsException();
		if (this.root.equals(this.NULL_NODE)) {
			this.root = new Node(c, this);
		} else {
			this.root = this.root.add(c, pos);
		}
	}

	/**
	 * 
	 * @return the number of nodes in this tree
	 */
	public int size() {
		return this.root.size(); // replace by a real calculation.
	}

	/**
	 * 
	 * @param pos
	 *            position of character to delete from this tree
	 * @return the character that is deleted
	 * @throws IndexOutOfBoundsException
	 */
	public char delete(int pos) throws IndexOutOfBoundsException {
		// Implementation requirement:
		// When deleting a node with two children, you normally replace the
		// node to be deleted with either its in-order successor or predecessor.
		// The tests assume assume that you will replace it with the
		// *successor*.
		if (pos < 0 || pos + 1 > this.size() || this.size() == 0)
			throw new IndexOutOfBoundsException();
		DropBox box = new DropBox('`');
		this.root = this.root.delete(pos, box);
		return box.getElement(); // replace by a real calculation.
	}

	/**
	 * This method operates in O(length*log N), where N is the size of this
	 * tree.
	 * 
	 * @param pos
	 *            location of the beginning of the string to retrieve
	 * @param length
	 *            length of the string to retrieve
	 * @return string of length that starts in position pos
	 * @throws IndexOutOfBoundsException
	 *             unless both pos and pos+length-1 are legitimate indexes
	 *             within this tree.
	 */
	public String get(int pos, int length) throws IndexOutOfBoundsException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			// can directly call get(pos) which return the element at each
			// position.
			sb.append(this.get(pos + i));
		}
		return sb.toString();
	}

	/**
	 * This method is provided for you, and should not need to be changed. If
	 * split() and concatenate() are O(log N) operations as required, delete
	 * should also be O(log N)
	 * 
	 * @param start
	 *            position of beginning of string to delete
	 * 
	 * @param length
	 *            length of string to delete
	 * @return an EditTree containing the deleted string
	 * @throws IndexOutOfBoundsException
	 *             unless both start and start+length-1 are in range for this
	 *             tree.
	 */
	public EditTree delete(int start, int length) throws IndexOutOfBoundsException {
		if (start < 0 || start + length >= this.size())
			throw new IndexOutOfBoundsException(
					(start < 0) ? "negative first argument to delete" : "delete range extends past end of string");
		EditTree t2 = this.split(start);
		EditTree t3 = t2.split(length);
		this.concatenate(t3);
		return t2;
	}

	/**
	 * Append (in time proportional to the log of the size of the larger tree)
	 * the contents of the other tree to this one. Other should be made empty
	 * after this operation.
	 * 
	 * @param other
	 * @throws IllegalArgumentException
	 *             if this == other
	 */
	public void concatenate(EditTree other) throws IllegalArgumentException {
		if (this == other)
			throw new IllegalArgumentException();

		if (this.height() >= other.height()) {
			try {
				// find out the leftmost element of the other tree.
				Node q = new Node(other.delete(0), this);
				this.root = this.root.takeFromTheOtherTree(q, other.root, this.height(), other.height());
				other.root = this.NULL_NODE;
			} catch (IndexOutOfBoundsException e) {
				// do nothing
			}
		} else {
			try {
				// find out the rightmost element of this tree.
				Node q = new Node(this.delete(this.size() - 1), this);
				this.root = other.root.joinTheOtherTree(q, this.root, this.height(), other.height(), this.size());
			} catch (IndexOutOfBoundsException e) {
				this.root = other.root;
			}
			other.root = this.NULL_NODE;
		}
	}

	/**
	 * This operation must be done in time proportional to the height of this
	 * tree.
	 * 
	 * @param pos
	 *            where to split this tree
	 * @return a new tree containing all of the elements of this tree whose
	 *         positions are >= position. Their nodes are removed from this
	 *         tree.
	 * @throws IndexOutOfBoundsException
	 */
	public EditTree split(int pos) throws IndexOutOfBoundsException {
		if (pos < 0 || pos + 1 > this.size() || this.size() == 0)
			throw new IndexOutOfBoundsException();

		Stack<Node> path = new Stack<Node>();
		Node current = this.root;

		// find the split node.
		while (current.rank != pos) {
			path.push(current);
			if (pos < current.rank) {
				current = current.left;
			} else {
				pos = pos - (current.rank + 1);
				current = current.right;
			}
		}

		// split up the old tree into two new trees.
		EditTree leftTree = new EditTree();
		EditTree rightTree = new EditTree();
		leftTree.root = current.left;
		rightTree.root = current.right;
		// set current to be the paste node (Node q).
		current.left = this.NULL_NODE;
		current.right = this.NULL_NODE;
		// insert current to be the leftmost node in the right tree, because all
		// the nodes at position >= pos need to be returned.
		rightTree.root = rightTree.root.joinTheOtherTree(current, this.NULL_NODE, -1, rightTree.height(), 0);

		while (!path.isEmpty()) {
			Node child = current;
			current = path.pop();
			if (child.equals(current.right)) {
				leftTree.root = leftTree.root.joinTheOtherTree(current, current.left, current.left.height(),
						leftTree.root.height(), current.left.size());
			} else {
				rightTree.root = current.right.joinTheOtherTree(current, rightTree.root, rightTree.root.height(),
						current.right.height(), rightTree.size());
			}
		}
		// replace the current tree with the leftTree, so that it can return the
		// rightTree, which contains all the node at position >= pos.
		this.root = leftTree.root;
		return rightTree;
	}

	/**
	 * Don't worry if you can't do this one efficiently.
	 * 
	 * @param s
	 *            the string to look for
	 * @return the position in this tree of the first occurrence of s; -1 if s
	 *         does not occur
	 */
	public int find(String s) {
		return this.find(s, 0);
	}

	/**
	 * 
	 * @param s
	 *            the string to search for
	 * @param pos
	 *            the position in the tree to begin the search
	 * @return the position in this tree of the first occurrence of s that does
	 *         not occur before position pos; -1 if s does not occur
	 */
	public int find(String s, int pos) {
		// en empty string always returns 0.
		if (s.isEmpty())
			return 0;
		int startIndex = -1;
		int endIndex = 0;
		String treeToString = this.toString();
		for (int i = pos; i < treeToString.length(); i++) {
			// "char matches" case.
			if (treeToString.charAt(i) == s.charAt(endIndex)) {
				// if the first character matches, set startIndex to 0;
				if (endIndex == 0) {
					startIndex = i;
				}
				endIndex++;
			} else {
				// once any character does not match, reset two index variables.
				startIndex = -1;
				endIndex = 0;
			}
			// return the startIndex when all character matches.
			if (endIndex == s.length()) {
				return startIndex;
			}
		}
		return startIndex;
	}

	/**
	 * @return The root of this tree.
	 */
	public Node getRoot() {
		return this.root;
	}
}
