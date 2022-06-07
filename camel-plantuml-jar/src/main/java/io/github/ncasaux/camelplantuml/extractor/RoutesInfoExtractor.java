package io.github.ncasaux.camelplantuml.extractor;

import io.github.ncasaux.camelplantuml.model.ConsumerInfo;
import io.github.ncasaux.camelplantuml.model.EndpointBaseUriInfo;
import io.github.ncasaux.camelplantuml.model.RouteInfo;
import io.github.ncasaux.camelplantuml.utils.ConsumerUtils;
import io.github.ncasaux.camelplantuml.utils.EndpointUtils;
import io.github.ncasaux.camelplantuml.utils.RouteUtils;
import org.apache.camel.support.EndpointHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.net.URLDecoder;
import java.util.*;

public class RoutesInfoExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutesInfoExtractor.class);

    public static void getRoutesInfo(MBeanServerConnection mbeanServer,
                                     HashMap<String, RouteInfo> routesInfo,
                                     ArrayList<ConsumerInfo> consumersInfo,
                                     HashMap<String, EndpointBaseUriInfo> endpointBaseUrisInfo) throws Exception {

        Set<ObjectName> routesSet = mbeanServer.queryNames(new ObjectName("org.apache.camel:type=routes,*"), null);
        List<ObjectName> routesList = new ArrayList<>();

        CollectionUtils.addAll(routesList, routesSet);
        Collections.sort(routesList);

        for (ObjectName on : routesList) {
            String routeState = (String) mbeanServer.getAttribute(on, "State");
            String routeId = (String) mbeanServer.getAttribute(on, "RouteId");

            LOGGER.debug("Processing routeId \"{}\"", routeId);
            if (!routeState.equalsIgnoreCase("Started")) {
                LOGGER.warn("Route with id \"{}\" is not started, associated processors may not have been created, diagram may be incomplete", routeId);
            }

            String endpointUri = (String) mbeanServer.getAttribute(on, "EndpointUri");
            String normalizedUri = EndpointHelper.normalizeEndpointUri(endpointUri);
            String endpointBaseUri = URLDecoder.decode(EndpointUtils.getEndpointBaseUri(normalizedUri, LOGGER), "UTF-8");
            String description = (String) mbeanServer.getAttribute(on, "Description");

            RouteInfo routeInfo = new RouteInfo(endpointBaseUri, description);
            RouteUtils.addRouteInfo(routesInfo, routeId, routeInfo, LOGGER);

            ConsumerInfo consumerInfo = new ConsumerInfo(routeId, endpointBaseUri, "from", false);
            ConsumerUtils.addConsumerInfo(consumersInfo, consumerInfo, LOGGER);

            EndpointBaseUriInfo endpointBaseUriInfo = new EndpointBaseUriInfo();
            EndpointUtils.addEndpointBaseUriInfo(endpointBaseUrisInfo, endpointBaseUri, endpointBaseUriInfo, LOGGER);
        }
    }
}
