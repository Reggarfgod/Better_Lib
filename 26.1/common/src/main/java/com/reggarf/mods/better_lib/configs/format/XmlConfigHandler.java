package com.reggarf.mods.better_lib.configs.format;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class XmlConfigHandler implements ConfigFormatHandler {
    public static final XmlConfigHandler INSTANCE = new XmlConfigHandler();

    @Override
    public ConfigFormat getFormat() {
        return ConfigFormat.XML;
    }

    @Override
    public Map<String, Object> read(File file) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        if (!file.exists()) return root;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        Element rootElement = doc.getDocumentElement();
        if (rootElement != null) {
            parseElement(rootElement, root);
        }
        return root;
    }

    private void parseElement(Element parent, Map<String, Object> target) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element childEl = (Element) node;
                String tagName = childEl.getTagName();

                boolean hasChildElements = false;
                NodeList grandchildren = childEl.getChildNodes();
                for (int j = 0; j < grandchildren.getLength(); j++) {
                    if (grandchildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        hasChildElements = true;
                        break;
                    }
                }

                if (hasChildElements) {
                    Map<String, Object> childMap = new LinkedHashMap<>();
                    parseElement(childEl, childMap);
                    target.put(tagName, childMap);
                } else {
                    String text = childEl.getTextContent() != null ? childEl.getTextContent().trim() : "";
                    target.put(tagName, parseXmlValue(text));
                }
            }
        }
    }

    private Object parseXmlValue(String raw) {
        if (raw.equalsIgnoreCase("true")) return Boolean.TRUE;
        if (raw.equalsIgnoreCase("false")) return Boolean.FALSE;
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            } else {
                return Long.parseLong(raw);
            }
        } catch (NumberFormatException ignored) {}
        return raw;
    }

    @Override
    public void write(File file, Map<String, Object> data, Map<String, String> comments) throws Exception {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("config");
        doc.appendChild(root);

        buildXml(doc, root, data, "", comments);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        try {
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (IllegalArgumentException ignored) {}

        try (FileOutputStream out = new FileOutputStream(file)) {
            transformer.transform(new DOMSource(doc), new StreamResult(out));
        }
    }

    @SuppressWarnings("unchecked")
    private void buildXml(Document doc, Element parent, Map<String, Object> data, String prefix, Map<String, String> comments) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String tag = sanitizeTag(entry.getKey());
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            String comment = comments != null ? comments.get(fullKey) : null;

            if (comment != null && !comment.trim().isEmpty()) {
                String safeComment = " " + comment.trim().replace("\n", " ").replace("--", "- -") + " ";
                parent.appendChild(doc.createComment(safeComment));
            }

            Element childEl = doc.createElement(tag);
            if (entry.getValue() instanceof Map) {
                buildXml(doc, childEl, (Map<String, Object>) entry.getValue(), fullKey, comments);
            } else {
                childEl.setTextContent(entry.getValue() == null ? "" : entry.getValue().toString());
            }
            parent.appendChild(childEl);
        }
    }

    private String sanitizeTag(String key) {
        return key.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
