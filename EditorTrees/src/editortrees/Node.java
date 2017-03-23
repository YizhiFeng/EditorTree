package editortrees;

import java.util.ArrayList;

// A node in a height-balanced binary tree with rank.
// Except for the NULL_NODE (if you choose to use one), one node cannot
// belong to two different trees.

public class Node {
	private EditTree et;
	private treatmentContainer tc;
	// used for finding the imbalance point
	private String childDirection;
	// the direction of the current node as the child of its parent

	public Node(EditTree et) {
		// used to create a NULL_NODE.
		this.et = et;
		this.left = null;
		this.right = null;
		this.parent = this.et.NULL_NODE;
	}

	public Node(char c, EditTree et) {
		this.element = c;
		this.et = et;
		this.rank = 0;
		this.tc = new treatmentContainer("balanced", null);
		this.balance = Code.SAME;
		this.left = this.et.NULL_NODE;
		this.right = this.et.NULL_NODE;
		this.parent = this.et.NULL_NODE;
		this.childDirection = "root";
	}

	enum Code {
		SAME, LEFT, RIGHT;
		// Used in the displayer and debug string
		public String toString() {
			switch (this) {
			case LEFT:
				return "/";
			case SAME:
				return "=";
			case RIGHT:
				return "\\";
			default:
				throw new IllegalStateException();
			}
		}
	}

	// The fields would normally be private, but for the purposes of this class,
	// we want to be able to test the results of the algorithms in addition to
	// the
	// "publicly visible" effects

	char element;
	Node left, right; // subtrees
	int rank; // inorder position of this node within its own subtree.
	Code balance;
	Node parent; // You may want this field.
	// Feel free to add other fields that you find useful

	// You will probably want to add several other methods

	// For the following methods, you should fill in the details so that they
	// work correctly

	/**
	 * 
	 * handle the case when the height of this tree is greater than or equal to
	 * that of the other tree in concatenate().
	 *
	 * @param q
	 * @param thisRoot
	 * @param thisHeight
	 * @param otherHeight
	 * @param thisOriginalSize
	 * @return the new root after concatenating
	 */
	public Node joinTheOtherTree(Node q, Node thisRoot, int thisHeight, int otherHeight, int thisOriginalSize) {
		// execute concatenation when the difference between the heights of two
		// tree is not over 1.
		if (otherHeight - thisHeight <= 1) {
			q.left = thisRoot;
			thisRoot.parent = q;
			thisRoot.childDirection = "left";
			q.right = this;
			this.parent = q;
			this.childDirection = "right";
			q.rank = thisRoot.size();
			if (thisHeight == otherHeight) {
				q.balance = Code.SAME;
			} else {
				q.balance = Code.RIGHT;
			}
			return q;
		}

		// keep going down leftward
		if (this.balance.equals(Code.RIGHT)) {
			this.left = this.left.joinTheOtherTree(q, thisRoot, thisHeight, otherHeight - 2, thisOriginalSize);
		} else {
			this.left = this.left.joinTheOtherTree(q, thisRoot, thisHeight, otherHeight - 1, thisOriginalSize);
			// rebalance if necessary
			if (this.balance.equals(Code.LEFT)) {
				if (this.left.balance.equals(Code.RIGHT))
					return doubleRight(this, this.left, this.left.right);
				if (this.left.balance.equals(Code.LEFT))
					return singleRotation("right", this, this.left);
			}
		}
		// changing each node's rank because of looping down left children.
		this.rank += thisOriginalSize + 1;
		return this;
	}

	/**
	 * 
	 * handle the case when the height of the other tree is greater than that of
	 * this tree in concatenate().
	 *
	 * @param q
	 * @param otherRoot
	 * @param thisHeight
	 * @param otherHeight
	 * @return the new root after concatenating
	 */
	public Node takeFromTheOtherTree(Node q, Node otherRoot, int thisHeight, int otherHeight) {
		// execute concatenation when the difference between the heights of two
		// tree is not over 1.
		if (thisHeight - otherHeight <= 1) {
			q.left = this;
			this.parent = q;
			this.childDirection = "left";
			q.right = otherRoot;
			if (!(otherRoot.left instanceof NullNode)) {
				otherRoot.parent = q;
				otherRoot.childDirection = "right";
			}
			// only changing the q's rank because of looping down right
			// children.
			q.rank = this.size();
			if (thisHeight == otherHeight) {
				q.balance = Code.SAME;
			} else {
				q.balance = Code.LEFT;
			}
			return q;
		}

		// keep going down rightward
		if (this.balance.equals(Code.LEFT)) {
			this.right = this.right.takeFromTheOtherTree(q, otherRoot, thisHeight - 2, otherHeight);
		} else {
			this.right = this.right.takeFromTheOtherTree(q, otherRoot, thisHeight - 1, otherHeight);
			// rebalance if necessary
			if (this.balance.equals(Code.RIGHT)) {
				if (this.right.balance.equals(Code.LEFT))
					return doubleLeft(this, this.right, this.right.left);
				if (this.right.balance.equals(Code.RIGHT))
					return singleRotation("left", this, this.right);
			}
		}
		return this;
	}

	/**
	 * 
	 * construct a new tree using the string s.
	 *
	 * @param s
	 * @return the root after constructing its children according to the order
	 *         of the string s.
	 */
	public Node forConstructorUsingString(String s) {
		if (s.length() == 0)
			return this.et.NULL_NODE;
		// start in the middle of the string because of in-order trasversal.
		Node newNode = new Node(s.charAt(s.length() / 2), this.et);
		// newNode has a rank that is equal to the length of the first half
		// subtring, which is the left subtree of newNode.
		newNode.rank = s.substring(0, s.length() / 2).length();
		// update the balance of newNode.
		if (s.length() % 2 == 0) {
			newNode.balance = Code.LEFT;
		} else {
			newNode.balance = Code.SAME;
		}
		// recursively call this method to set the left child and the right
		// child using each half of the string.
		newNode.left = newNode.forConstructorUsingString(s.substring(0, s.length() / 2));
		// set newNode be its children's parent.
		if (!(newNode.left instanceof NullNode))
			newNode.left.parent = newNode;
		newNode.right = newNode.forConstructorUsingString(s.substring(s.length() / 2 + 1));
		if (!(newNode.right instanceof NullNode))
			newNode.right.parent = newNode;
		return newNode;
	}

	/**
	 * 
	 * returns a treatmentContainer that specifies the rotation and executed
	 * node if needed.
	 *
	 * @param current
	 * @return a treatmentContainer that specifies the rotation and executed
	 *         node if needed.
	 */
	public treatmentContainer updateDeletionBalance(Node current) {
		while (!(current.parent instanceof NullNode)) {
			if (current.parent.balance.equals(Code.LEFT)) {
				// parent is tipped left.
				if (current.childDirection.equals("left")) {
					// when the deleted node is the left child.
					current.parent.balance = Code.SAME;
				} else {
					if (current.parent.left.balance.equals(Code.RIGHT)) {
						// when the deleted node is the right child and the
						// parent's left child is tipped right.
						return new treatmentContainer("dr", current.parent);
					}
					// when the parent's left child is tipped left or balanced.
					return new treatmentContainer("sr", current.parent);
				}
			} else if (current.parent.balance.equals(Code.RIGHT)) {
				// parent is tipped right
				if (current.childDirection.equals("right")) {
					// when the deleted node is the right child.
					current.parent.balance = Code.SAME;
				} else {
					if (current.parent.right.balance.equals(Code.LEFT)) {
						// when the deleted node is the left child and the
						// parent's right child is tipped right.
						return new treatmentContainer("dl", current.parent);
					}
					// when the parent's right child is tipped left or balanced.
					return new treatmentContainer("sl", current.parent);
				}
			} else {
				// parent is equally balanced
				if (current.childDirection.equals("right")) {
					current.parent.balance = Code.LEFT;
				} else {
					current.parent.balance = Code.RIGHT;
				}
				return new treatmentContainer("balanced", null);
			}
			current = current.parent;
		}
		return new treatmentContainer("balanced", null);
	}

	/**
	 * @return the smallest node in the right subtree.
	 */
	public char findSmallestOnRight() {
		if (this.left instanceof NullNode) {
			return this.element;
		}
		return this.left.findSmallestOnRight();
	}

	/**
	 * 
	 * returns the current node, or the new node if rotation is needed.
	 *
	 * @param tc
	 * @return the current node, or the new node if rotation is needed.
	 */
	public Node rotateIfNeededInDeletion(treatmentContainer tc) {
		if (!tc.treatment.equals("balanced")) {
			if (tc.treatment.equals("sl")) {
				Code oldBalance = this.right.balance;
				Node temp = singleRotation("left", this, this.right);
				if (oldBalance.equals(Code.SAME)) {
					// when the old balance of the child node is SAME, the new
					// parent and child's balance will not be SAME.
					temp.balance = Code.LEFT;
					temp.left.balance = Code.RIGHT;
				}
				return temp;
			}
			if (tc.treatment.equals("sr")) {
				Code oldBalance = this.left.balance;
				Node temp = singleRotation("right", this, this.left);
				if (oldBalance.equals(Code.SAME)) {
					// when the old balance of the child node is SAME, the new
					// parent and child's balance will not be SAME.
					temp.balance = Code.RIGHT;
					temp.right.balance = Code.LEFT;
				}
				return temp;
			}
			if (tc.treatment.equals("dl"))
				return doubleLeft(this, this.right, this.right.left);
			if (tc.treatment.equals("dr"))
				return doubleRight(this, this.left, this.left.right);
		}
		return this;
	}

	/**
	 * 
	 * returns the deleted node after rebalancing.
	 *
	 * @param pos
	 * @param box
	 * @return the deleted node after rebalancing.
	 */
	public Node delete(int pos, DropBox box) {
		if (pos == this.rank) {
			box.setElement(this.element);
			if (this.left instanceof NullNode) {
				if (this.right instanceof NullNode) {
					// when the deleted node has no children.
					this.parent.tc = updateDeletionBalance(this);
					return this.et.NULL_NODE;
				}
				// when the deleted node only has a right child.
				this.balance = Code.SAME;
				this.parent.tc = updateDeletionBalance(this);
				return this.right;
			}
			if (this.right instanceof NullNode) {
				// when the deleted node only has a left child.
				this.balance = Code.SAME;
				this.parent.tc = updateDeletionBalance(this);
				return this.left;
			}
			// when the deleted node has two children.
			this.element = this.right.findSmallestOnRight();
			// replace the deleted node with its successor.
			this.right = this.right.delete(0, new DropBox('`'));
			// pass another DropBox so that it won't change the element of the
			// deleted node.

		} else if (pos < this.rank) {
			this.rank--;
			this.left = this.left.delete(pos, box);
		} else {
			this.right = this.right.delete(pos - (this.rank + 1), box);
		}

		// update parent's balance and check if rotations are needed.
		if (!this.tc.treatment.equals("balanced")) {
			if (this.equals(this.tc.patient)) {
				if (!(this.parent instanceof NullNode)) {
					if (this.parent.balance.equals(Code.RIGHT)) {
						if (this.childDirection.equals("right")) {
							this.parent.balance = Code.SAME;
							this.parent.tc = updateDeletionBalance(this.parent);
						} else {
							this.parent.tc = updateDeletionBalance(this);
							// keeping going up and check if further rotations
							// are needed.
						}
					} else if (this.parent.balance.equals(Code.LEFT)) {
						if (this.childDirection.equals("left")) {
							this.parent.balance = Code.SAME;
							this.parent.tc = updateDeletionBalance(this.parent);
						} else {
							this.parent.tc = updateDeletionBalance(this);
							// keeping going up and check if further rotations
							// are needed.
						}
					}
				}
			} else {
				// when the patient is not the current node, pass the
				// treatmentConatiner to its parent.
				this.parent.tc = this.tc;
				this.tc = new treatmentContainer("balanced", null);
			}
		}

		treatmentContainer currentTC = this.tc;
		this.tc = new treatmentContainer("balanced", null);
		// reset the treatmentConatiner.
		return rotateIfNeededInDeletion(currentTC);
	}

	/**
	 * 
	 * returns the root after copying the whole tree.
	 *
	 * @param copy
	 * @param newParent
	 * @return the root after copying the whole tree.
	 */
	public Node copy(EditTree copy, Node newParent) {
		if (this instanceof NullNode)
			return copy.NULL_NODE;
		Node newNode = new Node(this.element, copy);
		newNode.childDirection = this.childDirection;
		newNode.parent = newParent;
		newNode.rank = this.rank;
		newNode.tc = this.tc;
		newNode.balance = this.balance;
		newNode.left = this.left.copy(copy, newNode);
		newNode.right = this.right.copy(copy, newNode);
		return newNode;
	}

	/**
	 * 
	 * return the element of the node at the position.
	 *
	 * @param pos
	 * @return the element of the node at the position.
	 */
	public char get(int pos) {
		if (pos < this.rank) {
			return this.left.get(pos);
		} else if (pos == this.rank) {
			return this.element;
		} else {
			return this.right.get(pos - (this.rank + 1));
		}
	}

	/**
	 * 
	 * handle the case when the imbalace node is not the current node and it
	 * needs to be rotate at parent node.
	 *
	 * @param fromLeft
	 * @return the current node after rotating if needed.
	 */
	public Node reviewTreatmentContainer(boolean fromLeft) {
		// if coming from the left child, the treatment can only be right
		// rotations.
		if (fromLeft) {
			if (!this.left.tc.treatment.equals("balanced")) {
				if (this.left.tc.treatment.equals("dr")) {
					if (this.equals(this.left.tc.patient) && isValid(this, this.left, this.left.right)) {
						this.left.tc = new treatmentContainer("balanced", null);
						return doubleRight(this, this.left, this.left.right);
					}
				}
				if (this.left.tc.treatment.equals("sr")) {
					if (this.equals(this.left.tc.patient)) {
						this.left.tc = new treatmentContainer("balanced", null);
						return singleRotation("right", this, this.left);
					}
				}
				// continue search for the imbalance point by going up to the
				// parents.
				this.tc = this.left.tc;
				this.left.tc = new treatmentContainer("balanced", null);
			}
		} else {
			// if coming from the right child, the treatment can only be left
			// rotations.
			if (!this.right.tc.treatment.equals("balanced")) {
				if (this.right.tc.treatment.equals("dl")) {
					// if (this.element == this.right.tc.patient.element) {
					if (this.equals(this.right.tc.patient) && isValid(this, this.right, this.right.left)) {
						this.right.tc = new treatmentContainer("balanced", null);
						return doubleLeft(this, this.right, this.right.left);
					}
				}
				if (this.right.tc.treatment.equals("sl")) {
					if (this.equals(this.right.tc.patient)) {
						this.right.tc = new treatmentContainer("balanced", null);
						return singleRotation("left", this, this.right);
					}
				}
				// continue search for the imbalance point by going up to the
				// parents.
				this.tc = this.right.tc;
				this.right.tc = new treatmentContainer("balanced", null);
			}
		}
		return this;
	}

	/**
	 * 
	 * return the root after adding a new node at the position.
	 *
	 * @param c
	 * @param pos
	 * @return the root after adding a new node at the position.
	 */
	public Node add(char c, int pos) {
		// go left of the root
		if (pos <= this.rank) {
			this.rank++;
			if (this.left instanceof NullNode) {
				// when the root has no left child.
				this.left = new Node(c, this.et);
				this.left.parent = this;
				this.left.childDirection = "left";
				isBalance(this.left); // no need to do rotation
				return this;
			}
			if (pos <= this.left.rank && this.left.left instanceof NullNode) {
				this.left.rank++;
				this.left.left = new Node(c, this.et);
				this.left.left.parent = this.left;
				this.left.left.childDirection = "left";
				treatmentContainer temptc = isBalance(this.left.left);
				if (!temptc.treatment.equals("balanced")) {
					if (temptc.treatment.equals("sr")) {
						if (this.equals(temptc.patient)) {
							return singleRotation("right", this, this.left);
						}
					}
					// if the imbalance point is not the current node, passed
					// back to its parent.
					this.tc = temptc;
					return this;
				}
				// return this if balance is remained.
				return this;
			}
			if (pos > this.left.rank && this.left.right instanceof NullNode) {
				this.left.right = new Node(c, this.et);
				this.left.right.parent = this.left;
				this.left.right.childDirection = "right";
				treatmentContainer temptc = isBalance(this.left.right);
				if (!temptc.treatment.equals("balanced")) {
					if (temptc.treatment.equals("dr")) {
						if (this.equals(temptc.patient) && isValid(this, this.left, this.left.right))
							return doubleRight(this, this.left, this.left.right);
					}
					this.tc = temptc;
					return this;
				}
				// return this if balance is remained.
				return this;
			}

			this.left = this.left.add(c, pos);

			// check if the left child tells the current node to do rotations.
			return this.reviewTreatmentContainer(true);

			/////////////////////////////////////////////////////////////////////////////////////
		}
		// go right of the root
		if (this.right instanceof NullNode) {
			this.right = new Node(c, this.et);
			this.right.parent = this;
			this.right.childDirection = "right";
			isBalance(this.right);
			return this;
		}
		if ((pos - this.rank - 1) > this.right.rank && this.right.right instanceof NullNode) {
			this.right.right = new Node(c, this.et);
			this.right.right.childDirection = "right";
			this.right.right.parent = this.right;
			treatmentContainer temptc = isBalance(this.right.right);
			if (!temptc.treatment.equals("balanced")) {
				if (temptc.treatment.equals("sl")) {
					if (this.equals(temptc.patient)) {
						return singleRotation("left", this, this.right);
					}
				}
				this.tc = temptc;
				return this;
			}
			// return this if balance is remained.
			return this;
		}
		if ((pos - this.rank - 1) <= this.right.rank && this.right.left instanceof NullNode) {
			this.right.rank++;
			this.right.left = new Node(c, this.et);
			this.right.left.childDirection = "left";
			this.right.left.parent = this.right;
			treatmentContainer temptc = isBalance(this.right.left);
			if (!temptc.treatment.equals("balanced")) {
				if (temptc.treatment.equals("dl") && isValid(this, this.right, this.right.left)) {
					if (this.equals(temptc.patient))
						return doubleLeft(this, this.right, this.right.left);
				}
				// if the imbalance point is not the current node, passed
				// back to its parent.
				this.tc = temptc;
				return this;
			}
			// return this if balance is remained.
			return this;
		}
		this.right = this.right.add(c, pos - (this.rank + 1));

		// check if the right child tells the current node to do rotations.
		return this.reviewTreatmentContainer(false);
	}

	/**
	 * 
	 * returns true if the grandParent, parent and child are non-null_node when
	 * doing double rotations.
	 *
	 * @param grandParent
	 * @param p
	 * @param child
	 * @return true if the grandParent, parent and child are non-null_node when
	 *         doing double rotations.
	 */
	public boolean isValid(Node grandParent, Node p, Node child) {
		if (!(grandParent instanceof NullNode)) {
			if (!(p instanceof NullNode)) {
				if (!(child instanceof NullNode))
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * return the root after adding a new node to the end.
	 *
	 * @param c
	 * @return the root after adding a new node to the end.
	 */
	public Node add(char c) {
		// if the root does not have a right child, no rotation is needed, add
		// a new node to the right and update its fields.
		if (this.right instanceof NullNode) {
			this.right = new Node(c, this.et);
			this.right.childDirection = "right";
			this.right.parent = this;
			isBalance(this.right);
			return this;
		}
		// when the root has a right child, add a new node to the right, check
		// balance and do rotations if needed.
		if (this.right.right instanceof NullNode) {
			this.right.right = new Node(c, this.et);
			this.right.right.childDirection = "right";
			this.right.right.parent = this.right;
			treatmentContainer temptc = isBalance(this.right.right);
			if (temptc.treatment.equals("sl")) {
				if (this.equals(temptc.patient)) {
					return singleRotation("left", this, this.right);
				}
				this.tc = temptc;
				// modify the treatmentContainer because the imbalance point is
				// not "this", so that this's parent can check if it is the
				// imbalance point.
				return this;
			}
			return this;
			// When height-balanced, make a flag so that it won't add another
			// new node to the right.
		}
		this.right = this.right.add(c);

		if (this.right.tc.treatment.equals("sl")) {
			if (this.equals(this.right.tc.patient)) {
				this.right.tc = new treatmentContainer("balanced", null);
				return singleRotation("left", this, this.right);
			}
			// continue search for the imbalance point by going up to the
			// parents.
			this.tc = this.right.tc;
			this.right.tc = new treatmentContainer("balanced", null);
			return this;
		}
		this.tc = new treatmentContainer("balanced", null);
		return this;
	}

	/**
	 * 
	 * a container that holds the type of rotation and the imbalance point.
	 *
	 * @author fengy2. Created Apr 19, 2016.
	 */
	class treatmentContainer {
		String treatment;
		Node patient;

		public treatmentContainer(String treatment, Node patient) {
			this.treatment = treatment;
			this.patient = patient;
		}
	}

	/**
	 * 
	 * returns a container that holds the type of rotation and the imbalance
	 * point.
	 *
	 * @param current
	 * @return a container that holds the type of rotation and the imbalance
	 *         point.
	 */
	public treatmentContainer isBalance(Node current) {
		while (!(current.parent instanceof NullNode)) {
			if (current.parent.balance.equals(Code.SAME)) {
				// tip Code to the insertionDirection and keep going up
				if (current.childDirection.equals("right")) {
					current.parent.balance = Code.RIGHT;
				} else if (current.childDirection.equals("left")) {
					current.parent.balance = Code.LEFT;
				}
			} else if (current.parent.balance.equals(Code.RIGHT)) {
				if (current.balance.equals(Code.RIGHT) && current.childDirection.equals("right"))
					// code tipped towards insertion point
					return new treatmentContainer("sl", current.parent); // imbalance
				if (current.balance.equals(Code.LEFT) && current.childDirection.equals("right"))
					// double left rotation is needed.
					return new treatmentContainer("dl", current.parent); // imbalance
				current.parent.balance = Code.SAME;
				// code tipped away from insertion point
				break; // stop
			} else if (current.parent.balance.equals(Code.LEFT)) {
				if (current.balance.equals(Code.LEFT) && current.childDirection.equals("left"))
					// code tipped towards insertion point
					return new treatmentContainer("sr", current.parent); // imbalance
				if (current.balance.equals(Code.RIGHT) && current.childDirection.equals("left"))
					// double right rotation is needed.
					return new treatmentContainer("dr", current.parent); // imbalance
				current.parent.balance = Code.SAME;
				// code tipped away from insertion point
				break; // stop
			}
			current = current.parent;
		}
		return new treatmentContainer("balanced", null);
	}

	/**
	 * 
	 * return the child after double-right rotation.
	 *
	 * @param grandParent
	 * @param parent
	 * @param child
	 * @return the child after double-right rotation.
	 */
	public Node doubleRight(Node grandParent, Node p, Node child) {

		// update the balance of each node based on the old balance of the
		// child.
		if (child.balance.equals(Code.SAME)) {
			p.balance = Code.SAME;
			grandParent.balance = Code.SAME;
		} else if (child.balance.equals(Code.LEFT)) {
			p.balance = Code.SAME;
			grandParent.balance = Code.RIGHT;
		} else {
			p.balance = Code.LEFT;
			grandParent.balance = Code.SAME;
		}
		child.balance = Code.SAME;

		child.childDirection = grandParent.childDirection;
		child.parent = grandParent.parent;

		p.right = child.left;
		p.right.parent = p;
		p.right.childDirection = "right";

		grandParent.left = child.right;
		grandParent.left.parent = grandParent;
		grandParent.left.childDirection = "left";

		child.left = p;
		child.left.parent = child;
		child.left.childDirection = "left";

		child.right = grandParent;
		child.right.parent = child;
		child.right.childDirection = "right";

		grandParent.rank -= (p.rank + child.rank + 2);
		child.rank += (p.rank + 1);

		this.et.increaseTRC();
		this.et.increaseTRC();

		return child;
	}

	/**
	 * 
	 * returns the child after double-left rotation
	 *
	 * @param grandParent
	 * @param parent
	 * @param child
	 * @return the child after double-left rotation
	 */
	public Node doubleLeft(Node grandParent, Node p, Node child) {

		// update the balance of each node based on the old balance of the
		// child.
		if (child.balance.equals(Code.SAME)) {
			p.balance = Code.SAME;
			grandParent.balance = Code.SAME;
		} else if (child.balance.equals(Code.LEFT)) {
			p.balance = Code.RIGHT;
			grandParent.balance = Code.SAME;
		} else {
			p.balance = Code.SAME;
			grandParent.balance = Code.LEFT;
		}
		child.balance = Code.SAME;

		child.childDirection = grandParent.childDirection;
		child.parent = grandParent.parent;

		p.left = child.right;
		p.left.childDirection = "left";

		grandParent.right = child.left;
		grandParent.right.parent = grandParent;
		grandParent.right.childDirection = "right";

		child.left = grandParent;
		child.left.parent = child;
		child.left.childDirection = "left";

		child.right = p;
		child.right.parent = child;
		child.right.childDirection = "right";

		p.rank -= (child.rank + 1);
		child.rank += (grandParent.rank + 1);

		this.et.increaseTRC();
		this.et.increaseTRC();

		return child;
	}

	/**
	 * 
	 * do the single rotation leftward or rightward.
	 *
	 * @param s
	 * @param parent
	 * @param child
	 * @return the child node after single rotation.
	 */
	public Node singleRotation(String s, Node p, Node child) {
		if (s.equals("left")) {
			// single left
			p.right = child.left;
			child.left = p;
			child.childDirection = p.childDirection;
			p.childDirection = "left";
			child.rank += p.rank + 1;
		} else {
			// single right
			p.left = child.right;
			child.right = p;
			child.childDirection = p.childDirection;
			p.childDirection = "right";
			p.rank -= child.rank + 1;
		}
		child.parent = p.parent;
		p.parent = child;
		p.balance = Code.SAME;
		child.balance = Code.SAME;
		this.et.increaseTRC();
		return child;
	}

	/**
	 * @return the height of the tree.
	 */
	public int height() {
		if (this instanceof NullNode)
			return -1;
		if (this.balance.equals(Code.LEFT))
			return this.left.height() + 1;
		return this.right.height() + 1;
	}

	/**
	 * @return the size of the tree.
	 */
	public int size() {
		if (this instanceof NullNode)
			return 0;
		// only recursively calling the right child
		return this.rank + 1 + this.right.size();
	}

	/**
	 * @return the string produced by an in-order traversal of this tree.
	 */
	@Override
	public String toString() {
		if (this instanceof NullNode)
			return "";
		return this.left.toString() + this.element + this.right.toString();
	}

	/**
	 * 
	 * modify the passed arraylist with every node's element, rank and balance,
	 * in pre-order traversal
	 *
	 * @param ar
	 */
	public void toDebugString(ArrayList<String> ar) {
		if (this instanceof NullNode)
			return;
		String result = "" + this.element + this.rank + this.balance;
		ar.add(result);
		this.left.toDebugString(ar);
		this.right.toDebugString(ar);
	}

}