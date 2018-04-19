package net.ssehub.kernel_haven.incremental.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

// TODO: Auto-generated Javadoc
/**
 * The Class Marshaller. Helper class to convert any java object with
 * corresponding annotations from or to a json-String.
 * <p>
 * Simple example for a parseable class where all fields should be represented
 * in json:
 * 
 * <pre>
 * &#64;XmlRootElement
 * &#64;XmlAccessorType(XmlAccessType.FIELD)
 * public class MyClass {
 *    ...
 * }
 * </pre>
 */
public class Marshaller {

	/**
	 * Marshal to json.
	 *
	 * @param object
	 *            the object
	 * @param javaClass
	 *            the java class of the object
	 * @return the string
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public static String marshalToJson(Object object, Class<?> javaClass) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { javaClass }, properties);

		javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshal(object, baos);
		byte[] content = baos.toByteArray();
		return new String(content);
	}

	/**
	 * Unmarshal from json.
	 *
	 * @param json
	 *            the json
	 * @param javaClass
	 *            the java class of the object
	 * @return the object
	 * @throws JAXBException
	 *             the JAXB exception
	 */
	public static Object unmarshalFromJson(String json, Class<?> javaClass) throws JAXBException {
		Map<String, Object> properties = new HashMap<>();
		properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
		properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

		JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { javaClass }, properties);

		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		byte[] content = json.getBytes();

		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		Object copy = unmarshaller.unmarshal(bais);

		return copy;
	}
}