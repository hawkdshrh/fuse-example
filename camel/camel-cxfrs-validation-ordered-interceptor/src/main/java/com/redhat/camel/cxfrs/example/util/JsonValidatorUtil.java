/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.camel.cxfrs.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.cxf.interceptor.Fault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhawkins
 */
public class JsonValidatorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(JsonValidatorUtil.class);

    public static boolean validateSchema(InputStream inputStream, String schemaLocation) {

        // read JSON Shema from a file
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        
        InputStream is = classloader.getResourceAsStream(schemaLocation);

        try {
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

            JsonNode schema = JsonLoader.fromReader(new InputStreamReader(is));
            final JsonSchema requestSchema = factory.getJsonSchema(schema);

            JsonNode json = JsonLoader.fromReader(new InputStreamReader(inputStream));
            ProcessingReport report;
            report = requestSchema.validate(json);
            if (!report.isSuccess()) {
                throw new Fault(new RuntimeException(report.toString()));
            }

        } catch (ProcessingException | IOException e) {
            LOG.error(e.getLocalizedMessage());
        }
        return true;
    }

}
