package porua.plugin.utility;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PluginDomUtility {
	public static Document xmlToDom(String xml) {
		Document xmlDoc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
			xmlDoc.getDocumentElement().normalize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlDoc;

	}

	public static String domToXml(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", new Integer(2));
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Recursively find the child node in parent node.
	 * 
	 * @param node
	 *            The node to be operated.
	 * @param size
	 *            Exit condition.
	 * @param index
	 *            Index of the node in the children of the node.
	 * @return
	 */
	public static Node iterate(Node node, List<Integer> hierarchy, int index) {
		if (index < hierarchy.size()) {
			Node n = node.getChildNodes().item(hierarchy.get(index));
			index = index + 1;
			return iterate(n, hierarchy, index);
		} else {
			return node;
		}
	}
}
