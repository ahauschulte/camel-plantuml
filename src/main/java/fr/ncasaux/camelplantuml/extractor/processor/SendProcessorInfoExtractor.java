package fr.ncasaux.camelplantuml.extractor.processor;

import fr.ncasaux.camelplantuml.model.ProducerInfo;
import fr.ncasaux.camelplantuml.utils.ListUtils;
import org.apache.camel.util.URISupport;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fr.ncasaux.camelplantuml.utils.EndpointUtils.getEndpointBaseUri;

public class SendProcessorInfoExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendProcessorInfoExtractor.class);

    public static void getProcessorsInfo(MBeanServer mbeanServer,
                                         ArrayList<ProducerInfo> producersInfo)
            throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, URISyntaxException, UnsupportedEncodingException {

        QueryExp exp = Query.eq(Query.classattr(), Query.value("org.apache.camel.management.mbean.ManagedSendProcessor"));
        Set<ObjectName> processorsSet = mbeanServer.queryNames(new ObjectName("org.apache.camel:type=processors,*"), exp);
        List<ObjectName> processorsList = new ArrayList<>();
        CollectionUtils.addAll(processorsList, processorsSet);

        for (ObjectName on : processorsList) {
            String destination = (String) mbeanServer.getAttribute(on, "Destination");
            String normalizedUri = URISupport.normalizeUri(destination);
            String endpointBaseUri = URLDecoder.decode(getEndpointBaseUri(normalizedUri, LOGGER), "UTF-8");

            ProducerInfo producerInfo = new ProducerInfo((String) mbeanServer.getAttribute(on, "RouteId"),
                    endpointBaseUri, "to", false);
            ListUtils.addProducerInfoIfNotInList(producersInfo, producerInfo, LOGGER);
        }
    }
}