package dii.vrp.data;

import java.util.Iterator;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;

import dii.vrp.util.XMLParser;


/**
 * Implements an XML data reader for the forma we will use in the TP
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 17, 2016
 *
 */
public class XMLDataReader implements AutoCloseable{

	private Document xmlArcs=null;
	private Document xmlNodes=null;
	private int n;
	
	public XMLDataReader(String pathnameArcs, String pathnameNodes){
		xmlArcs=XMLParser.parse(pathnameArcs);	
		if(xmlArcs==null)
			throw new IllegalStateException("The arc file cannot be read");
		xmlNodes=XMLParser.parse(pathnameNodes);	
		if(xmlNodes==null)
			throw new IllegalStateException("The arc file cannot be read");
		n=xmlNodes.getRootElement().getChildren().size();
	}
	
	
	public IDistanceMatrix readDistances() {

		//Initialize the distane matrix
		IDistanceMatrix dm=new ArrayDistanceMatrix(this.n);
		
		//Iterate over the list of arcs and extract the distances 
		Iterator<Element> it=xmlArcs.getRootElement().getChildren().iterator();
		while(it.hasNext()){
			Element arc=it.next();
			try {
				int tail=arc.getAttribute("start").getIntValue();
				int head=arc.getAttribute("arrival").getIntValue();
				double distance=Double.valueOf(arc.getValue());
				dm.setDistance(tail, head, distance);
				dm.setDistance(head, tail, distance);
			} catch (DataConversionException e) {
				e.printStackTrace();
				throw new IllegalStateException("The arc file cannot be read" + arc.toString());
			}
		}

		return dm;
	}

	public IDemands readDemands() {

		//Initialize the demands
		IDemands demands=new ArrayDemands(this.n);

		//Iterate over the list of arcs and extract the distances 
		Iterator<Element> it=xmlNodes.getRootElement().getChildren().iterator();
		while(it.hasNext()){
			Element node=it.next();
			int id;
			try {
				id = node.getAttribute("id").getIntValue();
				double demand=Double.valueOf(node.getChild("demand").getValue());
				demands.setDemand(id, demand);
			} catch (DataConversionException e){
				e.printStackTrace();
				throw new IllegalStateException("The node file cannot be read");
			}
		}
		return demands;
	}

	public int readNbNodes() {
		return this.n;
	}

	@Override
	public void close() throws Exception {
		this.xmlArcs=null;
		this.xmlNodes=null;
	}


}
