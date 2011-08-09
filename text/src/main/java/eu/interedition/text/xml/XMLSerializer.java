package eu.interedition.text.xml;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.bcel.internal.generic.LoadClass;
import com.sun.xml.internal.fastinfoset.util.LocalNameQualifiedNamesMap;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.event.ExceptionPropagatingAnnotationEventAdapter;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.util.Annotations;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.util.Annotations.DEFAULT_ORDERING;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLSerializer {

  private AnnotationEventSource eventSource;

  @Required
  public void setEventSource(AnnotationEventSource eventSource) {
    this.eventSource = eventSource;
  }

  public void serialize(final ContentHandler xml, Text text, final XMLSerializerConfiguration config) throws XMLStreamException, IOException {
    try {
      eventSource.listen(new SerializingListener(xml, config), text, config.getQuery());
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
      throw Throwables.propagate(t);
    }
  }

  private class SerializingListener extends ExceptionPropagatingAnnotationEventAdapter {
    private final ContentHandler xml;
    private final XMLSerializerConfiguration config;
    private final Map<URI, String> namespaceMappings = Maps.newHashMap();

    private boolean mappingsWritten = false;

    private SerializingListener(ContentHandler xml, XMLSerializerConfiguration config) {
      this.xml = xml;
      this.config = config;
    }

    @Override
    protected void doStart() throws Exception {
      for (Map.Entry<String, URI> mapping : config.getNamespaceMappings().entrySet()) {
        namespaceMappings.put(mapping.getValue(), mapping.getKey());
      }
      namespaceMappings.put(URI.create(XMLConstants.XML_NS_URI), XMLConstants.XML_NS_PREFIX);
      namespaceMappings.put(URI.create(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), XMLConstants.XMLNS_ATTRIBUTE);

      xml.startDocument();

      final QName rootName = config.getRootName();
      if (rootName != null) {
        startElement(rootName, Collections.<QName, String>emptyMap());
      }
    }

    @Override
    protected void doStart(int offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : DEFAULT_ORDERING.immutableSortedCopy(annotations.keySet())) {
        startElement(a.getName(), annotations.get(a));
      }
    }

    @Override
    protected void doEmpty(int offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : DEFAULT_ORDERING.immutableSortedCopy(annotations.keySet())) {
        final QName name = a.getName();
        startElement(name, annotations.get(a));
        endElement(name);
      }
    }

    @Override
    protected void doEnd(int offset, Map<Annotation, Map<QName, String>> annotations) throws Exception {
      for (Annotation a : Annotations.DEFAULT_ORDERING.reverse().immutableSortedCopy(annotations.keySet())) {
        endElement(a.getName());
      }
    }

    @Override
    protected void doText(Range r, String text) throws Exception {
      final char[] chars = text.toCharArray();
      xml.characters(chars, 0, chars.length);
    }

    @Override
    protected void doEnd() throws Exception {
      final QName rootName = config.getRootName();
      if (rootName != null) {
        endElement(rootName);
      }
      xml.endDocument();
    }

    private void startElement(QName name, Map<QName, String> attributes) throws SAXException {
      final Map<QName, String> nsAttributes = Maps.newHashMap();
      if (!mappingsWritten) {
        for (Map.Entry<URI, String> mapping : namespaceMappings.entrySet()) {
          final String prefix = mapping.getValue();
          final String uri = mapping.getKey().toString();
          if (XMLConstants.XML_NS_PREFIX.equals(prefix) || XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            continue;
          }
          if (prefix.length() == 0) {
            nsAttributes.put(new SimpleQName((URI) null, XMLConstants.XMLNS_ATTRIBUTE), uri);
          } else {
            nsAttributes.put(new SimpleQName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix), uri);
          }
        }
        mappingsWritten = true;
      }
      for (QName n : Iterables.concat(attributes.keySet(), Collections.singleton(name))) {
        final URI ns = n.getNamespaceURI();
        if (ns == null || namespaceMappings.containsKey(ns)) {
          continue;
        }
        int count = 0;
        String newPrefix = "ns" + count;
        while (true) {
          if (!namespaceMappings.containsKey(newPrefix)) {
            break;
          }
          newPrefix = "ns" + (++count);
        }
        namespaceMappings.put(ns, newPrefix);
        nsAttributes.put(new SimpleQName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, newPrefix), ns.toString());
      }

      final Map<QName, String> mergedAttributes = Maps.newLinkedHashMap();
      mergedAttributes.putAll(nsAttributes);
      mergedAttributes.putAll(attributes);
      xml.startElement(toNamespace(name.getNamespaceURI()), name.getLocalName(), toQNameStr(name), toAttributes(mergedAttributes));
    }

    private void endElement(QName name) throws SAXException {
      xml.endElement(toNamespace(name.getNamespaceURI()), name.getLocalName(), toQNameStr(name));
    }

    private String toNamespace(URI uri) {
      return (uri == null ? "" : uri.toString());
    }

    private String toQNameStr(QName name) {
      final URI ns = name.getNamespaceURI();
      final String localName = name.getLocalName();

      if (ns == null) {
        return localName;
      } else {
        final String prefix = namespaceMappings.get(ns);
        return (prefix.length() == 0 ? localName : prefix + ":" + localName);
      }
    }

    private QName toQName(String str) {
      final int colon = str.indexOf(':');
      return (colon >= 0 ? toQName(str.substring(0, colon), str.substring(colon + 1)) : toQName(null, str));
    }

    private QName toQName(String uri, String localName) {
      return new SimpleQName(URI.create(uri), localName);
    }

    private Attributes toAttributes(final Map<QName, String> attributes) {
      return new Attributes() {
        final List<QName> names = Lists.newArrayList(attributes.keySet());

        public int getLength() {
          return names.size();
        }

        public String getURI(int index) {
          return toNamespace(names.get(index).getNamespaceURI());
        }

        public String getLocalName(int index) {
          return names.get(index).getLocalName();
        }

        public String getQName(int index) {
          return toQNameStr(names.get(index));
        }

        public String getType(int index) {
          return (index >= 0 && index < names.size() ? "CDATA" : null);
        }

        public String getValue(int index) {
          return attributes.get(names.get(index));
        }

        public int getIndex(String uri, String localName) {
          return names.indexOf(toQName(uri, localName));
        }

        public int getIndex(String qName) {
          return names.indexOf(toQName(qName));
        }

        public String getType(String uri, String localName) {
          return names.indexOf(toQName(uri, localName)) >= 0 ? "CDATA" : null;
        }

        public String getType(String qName) {
          return names.indexOf(toQName(qName)) >= 0 ? "CDATA" : null;
        }

        public String getValue(String uri, String localName) {
          return attributes.get(toQName(uri, localName));
        }

        public String getValue(String qName) {
          return attributes.get(toQName(qName));
        }
      };
    }
  }
}