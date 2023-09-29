/*
 * Copyright (c) 2023 Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 */

package org.mmarini.hilbert.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mmarini.yaml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.mmarini.yaml.Utils.objectMapper;

/**
 * Loads the society from yaml resources
 */
public class StatusSerde {
    public static final String VERSION = "0.1";
    private static final Logger logger = LoggerFactory.getLogger(StatusSerde.class);
    private static final String STATUS_SCHEMA = "/status-schema.yml";

    /**
     * Returns the society from yaml resource
     *
     * @param file the yaml resource
     * @throws IOException in case of error
     */
    public static Status fromFile(String file) throws IOException {
        return fromJson(Utils.fromFile(file));
    }

    /**
     * Returns the society and the ule engine from json node
     *
     * @param node the json node
     */
    public static Status fromJson(JsonNode node) {
        JsonSchemas.instance().validateOrThrow(node, STATUS_SCHEMA);
        return new Status(
                node.path("population").asInt(),
                node.path("farmerPrefs").asDouble(),
                node.path("researcherPrefs").asDouble(),
                node.path("educatorPrefs").asDouble(),
                node.path("inactivePrefs").asDouble(),
                node.path("foodPrefs").asDouble(),
                node.path("researchPrefs").asDouble(),
                node.path("educationPrefs").asDouble(),
                node.path("settlementPrefs").asDouble(),
                node.path("technology").asDouble());
    }

    /**
     * Returns the json node of status
     *
     * @param status the status
     */
    public static JsonNode toJson(Status status) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("version", VERSION);
        node.put("population", status.getPopulation());
        node.put("farmerPrefs", status.getFarmerPrefs());
        node.put("researcherPrefs", status.getResearcherPrefs());
        node.put("educatorPrefs", status.getEducatorPrefs());
        node.put("inactivePrefs", status.getInactivePrefs());
        node.put("foodPrefs", status.getFoodPrefs());
        node.put("researchPrefs", status.getResearchPrefs());
        node.put("educationPrefs", status.getEducationPrefs());
        node.put("settlementPrefs", status.getSettlementPrefs());
        node.put("technology", status.getTechnology());
        return node;
    }

    /**
     * Writes status yaml to file
     *
     * @param file   the file
     * @param status the status
     * @throws IOException in case of error
     */
    public static void write(File file, Status status) throws IOException {
        logger.atDebug().log("Write {}", file);
        objectMapper.writeValue(file, toJson(status));
    }
}
