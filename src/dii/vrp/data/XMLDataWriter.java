package dii.vrp.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import dii.vrp.tp.VRPSolution;
import dii.vrp.util.XMLParser;

public class XMLDataWriter {

	public static void writeSolToXML(String pathnameNodes, String pathnameSol, VRPSolution s){
		Document xmlNodes=XMLParser.parse(pathnameNodes);
		if(xmlNodes==null)
			throw new IllegalStateException("The node file cannot be read");
		
		HashMap<Integer,Element> nodes=new HashMap<>();
		Iterator<Element> it=xmlNodes.getRootElement().getChildren().iterator();
		while(it.hasNext()){
			Element e=it.next();
			try {
				nodes.put(e.getAttribute("id").getIntValue(),e);
			} catch (DataConversionException e1) {
				e1.printStackTrace();
			}
		}
		
		Document xmlSol=new Document();
		Element root=new Element("sol");
		xmlSol.addContent(root);
		for(int r=0;r<s.size();r++){
			Element er=new Element("route");
			er.setAttribute("id",String.valueOf(r));
			for(int i=0;i<s.size(r);i++){				
				er.addContent(nodes.get(s.getNode(r, i)).clone());
			}
			root.addContent(er);
		}
		
		XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        try {
			xmlOutputter.output(xmlSol, new FileOutputStream(pathnameSol));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
