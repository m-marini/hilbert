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
import org.mmarini.Tuple2;
import org.mmarini.yaml.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads the society from yaml resources
 */
public class RulesSerde {
    public static final String RULES_SCHEMA = "/rules-schema.yml";
    private static final Logger logger = LoggerFactory.getLogger(RulesSerde.class);

    /**
     * Returns the society from yaml resource
     *
     * @param file the yaml resource
     * @throws IOException in case of error
     */
    public static Function<Status, Tuple2<Status, Supplier<Map<String, Number>>>> fromFile(String file) throws IOException {
        return fromJson(Utils.fromFile(file));
    }

    /**
     * Returns the society and the ule engine from json node
     *
     * @param node the json node
     */
    public static Function<Status, Tuple2<Status, Supplier<Map<String, Number>>>> fromJson(JsonNode node) {
        logger.atDebug().log("from json");
        // Validates the document
        JsonSchemas.instance().validateOrThrow(node, RULES_SCHEMA);
        // Loads all the rules
        long seed = node.path("seed").asLong(0);
        ExtRandom random = seed != 0 ? new ExtRandom(seed) : new ExtRandom();
        List<BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>>> rules = List.of(
                loadOverSettlementRule(node, random),
                loadFoodProductionRule(node, random),
                loadResearchRule(node, random),
                loadEducationRule(node, random)
        );
        UnaryOperator<Status> normalize = loadNormalizationRule(node);
        double dt = loadTimeInterval(node);
        // Load demography
        return status -> {
            // Creates the demography
            // Apply the rules
            Supplier<Collection<Tuple2<String, Number>>> initKpi = status::getKpi;
            List<Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> partials = Stream.concat(
                            Stream.of(Tuple2.of(status, initKpi)),
                            rules.stream()
                                    .map(f -> f.apply(status, dt)))
                    .collect(Collectors.toList());

            Status newStatus = normalize.apply(Status.sum(partials.stream().map(Tuple2::getV1).toArray(Status[]::new)));
            Supplier<Map<String, Number>> kpi = () -> partials.stream()
                    .flatMap(t -> t._2.get().stream())
                    .collect(Tuple2.toMap());
            return Tuple2.of(newStatus, kpi);
        };
    }

    /**
     * Returns the food production rule
     *
     * @param node   the json main node
     * @param random the random number generator
     */
    static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> loadEducationRule(JsonNode node, ExtRandom random) {
        JsonNode foodNode = node.path("education");
        double productivity = foodNode.path("productivity").asDouble();
        double demand = foodNode.path("demand").asDouble();
        double timeConstant = foodNode.path("timeConstant").asDouble();
        return HilbertRules.educationRule(random, loadResources(node), productivity, demand, timeConstant);
    }

    /**
     * Returns the food production rule
     *
     * @param node   the json main node
     * @param random the random number generator
     */
    static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> loadFoodProductionRule(JsonNode node, ExtRandom random) {
        JsonNode foodNode = node.path("foodProduction");
        double productivity = foodNode.path("productivity").asDouble();
        double demand = foodNode.path("demand").asDouble();
        double deathTimeConstant = foodNode.path("deathTimeConstant").asDouble();
        double birthTimeConstant = foodNode.path("birthTimeConstant").asDouble();
        double resources = loadResources(node);
        return HilbertRules.foodProductionRule(random, resources, productivity, demand, deathTimeConstant, birthTimeConstant);
    }

    static UnaryOperator<Status> loadNormalizationRule(JsonNode node) {
        double minTechnology = node.path("minTechnology").asDouble();
        return status -> status.normalize(minTechnology);
    }

    /**
     * Returns the over settlement rule
     *
     * @param node   the json main node
     * @param random the random number generator
     */
    static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> loadOverSettlementRule(JsonNode node, ExtRandom random) {
        JsonNode settlementNode = node.path("overSettlement");
        double density = settlementNode.path("density").asDouble();
        double deadTimeConstant = settlementNode.path("deathTimeConstant").asDouble();
        return HilbertRules.overSettlement(random, loadResources(node), density, deadTimeConstant);
    }

    /**
     * Returns the research rule
     *
     * @param node   the json main node
     * @param random the random number generator
     */
    static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> loadResearchRule(JsonNode node, ExtRandom random) {
        JsonNode researchNode = node.path("research");
        double productivity = researchNode.path("productivity").asDouble();
        double cost = researchNode.path("cost").asDouble();
        double quantum = researchNode.path("quantum").asDouble();
        return HilbertRules.researchRule(random, loadResources(node), productivity, cost, quantum);
    }

    /**
     * Returns the demography rule
     *
     * @param node the json node
     */
    static double loadResources(JsonNode node) {
        return node.path("resources").asDouble();
    }

    /**
     * Returns the time interval from json node
     *
     * @param node the json node
     */
    private static double loadTimeInterval(JsonNode node) {
        return node.path("timeInterval").asDouble();
    }
}
