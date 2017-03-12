package dii.vrp.data;

/**
 * Defines an interface to a parser that reads the number of nodes on an instance
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 17, 2016
 *
 */
public interface INbNodeParser {
	/**
	 * @param pathname the file holding the information
	 * @return the number of nodes in the instance. By convention this number includes the depot(s).
	 */
	public int readNbNodes(String pathname);

}
