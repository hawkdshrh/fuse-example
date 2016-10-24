package com.redhat.gss.test.templates.scr;

import com.redhat.gss.test.factory.CamelComponentFactory;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.scr.AbstractCamelRunner;
import org.apache.camel.spi.ComponentResolver;
import org.apache.felix.scr.annotations.*;
import com.redhat.gss.test.templates.scr.simple.ScrRouteBuilder;

import java.util.ArrayList;
import java.util.List;
import org.apache.camel.model.ModelCamelContext;

@Component(label = RouteBuilderScrRunner.COMPONENT_LABEL,
        policy = ConfigurationPolicy.REQUIRE,
        description = RouteBuilderScrRunner.COMPONENT_DESCRIPTION,
        immediate = true, metatype = true)
@Properties({
    @Property(name = "camelContextId", value = "FF_SimpleSCR"),
    @Property(name = "active", value = "true"),
    @Property(name = "exUnHandledExtension", value = "UNK"),
    @Property(name = "outDestination", value = "qOUT"),
    @Property(name = "sourceDestination", value = "qIN"),
    @Property(name = "Version", value = "1.0.0")
})
@References({
    @Reference(name = "camelComponent",
            referenceInterface = ComponentResolver.class,
            cardinality = ReferenceCardinality.MANDATORY_MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY,
            bind = "gotCamelComponent",
            unbind = "lostCamelComponent"),
    @Reference(name = "sourceComponentFactory",
            referenceInterface = CamelComponentFactory.class,
            target = "(provider=activemq-out-consumer)",
            bind = "setSourceComponentFactory",
            unbind = "unsetSourceComponentFactory"),
    @Reference(name = "destComponentFactory",
            referenceInterface = CamelComponentFactory.class,
            target = "(provider=activemq-out-producer)",
            bind = "setDestComponentFactory",
            unbind = "unsetDestComponentFactory")})

public class RouteBuilderScrRunner extends AbstractCamelRunner {

    public static final String COMPONENT_LABEL = "com.redhat.gss.test.templates.scr";
    public static final String COMPONENT_DESCRIPTION = "This is the test scr route builder.";

    private CamelComponentFactory destComponentFactory;
    private CamelComponentFactory sourceComponentFactory;

    @Override
    protected List<RoutesBuilder> getRouteBuilders() {

        if (null != sourceComponentFactory) {
            this.getContext().addComponent("source-component", sourceComponentFactory.createComponent());
        } else {
            log.warn("Null Source Component Factory - not registering ActiveMQ Component");
        }
        if (null != destComponentFactory) {
            this.getContext().addComponent("dest-component", destComponentFactory.createComponent());
        } else {
            log.warn("Null Destination Component Factory - not registering ActiveMQ Component");
        }

        List<RoutesBuilder> routesBuilders = new ArrayList<>();

        ScrRouteBuilder scrRouteBuilder = new ScrRouteBuilder();

        routesBuilders.add(scrRouteBuilder);
        return routesBuilders;
    }

    void setDestComponentFactory(CamelComponentFactory destComponentFactory) {
        this.destComponentFactory = destComponentFactory;
    }

    void unsetDestComponentFactory(CamelComponentFactory destComponentFactory) {
    }

    void setSourceComponentFactory(CamelComponentFactory sourceComponentFactory) {
        this.sourceComponentFactory = sourceComponentFactory;
    }

    void unsetSourceComponentFactory(CamelComponentFactory sourceComponentFactory) {
    }
    
    @Override
    public ModelCamelContext getContext() {
        return context;
    }
}
