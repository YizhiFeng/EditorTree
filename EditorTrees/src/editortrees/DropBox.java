package editortrees;

/**
 * 
 * This is a container that is used for delete(int pos). It contains the element
 * of the deleted node at pos.
 *
 * @author fengy2. Created Apr 24, 2016.
 */
public class DropBox {
	private char element;

	public DropBox(char element) {
		this.element = element;
	}

	/**
	 * @return the element of DropBox.
	 */
	public char getElement() {
		return this.element;
	}

	/**
	 * 
	 * modify the element of the DropBox.
	 *
	 * @param element
	 */
	public void setElement(char element) {
		this.element = element;
	}
}
