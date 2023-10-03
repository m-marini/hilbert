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
import org.junit.jupiter.api.Test;
import org.mmarini.Tuple2;
import org.mmarini.hilbert.TestFunctions;
import org.mmarini.yaml.Utils;
import org.mockito.hamcrest.MockitoHamcrest;

import java.io.IOException;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.Math.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RulesSerdeTest {

    @Test
    void loadEducationRule() throws IOException {
        // Given ...
        double productivity = 1;
        double demand = 1;
        double timeConstant = 1;
        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "resources: 130",
                "education:",
                "  productivity: " + productivity,
                "  demand: " + demand,
                "  timeConstant: " + timeConstant
        ));
        int educators = 100;
        int others = 10;
        int population = educators + 4 * others;
        double education = 100;
        double otherRes = 10;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status = Status.create(
                others, others, educators, others, others,
                otherRes, otherRes, education, otherRes, otherRes,
                technology
        );

        ExtRandom random = mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3);
        double lambda = population / demand - educators * productivity * eff / demand / timeConstant;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = RulesSerde.loadEducationRule(jsonNode, random);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> edu = rule.apply(status, 1d);

        // Then ...
        assertThat(edu._1.getTechnology(), closeTo(-technology * 3d / population, 1e-6));
        verify(random).nextPoisson(MockitoHamcrest.doubleThat(closeTo(lambda, 1e-6)));
    }

    @Test
    void loadFoodProductionRule() throws IOException {
        // Given ...
        double food = 60;
        double otherRes = 10;
        double resources = food + 4 * otherRes;

        double timeConstant = 1;
        double productivity = 1;
        double demand = 1;

        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "resources: " + resources,
                "foodProduction:",
                "  productivity: " + productivity,
                "  demand: " + demand,
                "  deathTimeConstant: " + timeConstant,
                "  birthTimeConstant: " + timeConstant
        ));

        int farmers = 60;
        int others = 10;
        int pop = farmers + 4 * others;

        double timeInterval = 1;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status0 = Status.create(
                farmers, others, others, others, others,
                food, otherRes, otherRes, otherRes, otherRes,
                technology);

        double kfPop = eff * productivity * farmers / pop / demand;
        double kfRes = eff * food / resources * resources / pop / demand;
        double kf = min(kfPop, kfRes);
        double lambda = max(0, pop * (1 - kf)) * timeInterval / timeConstant;

        ExtRandom random = mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = RulesSerde.loadFoodProductionRule(jsonNode, random);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> deaths = rule.apply(status0, timeInterval);

        // Then ...
        assertEquals(Status.population(-3), deaths._1);
        verify(random).nextPoisson(MockitoHamcrest.doubleThat(closeTo(lambda, 1e-6)));
    }

    @Test
    void loadHealthRule() throws IOException {
        // Given ...
        double health = 100;
        double otherRes = 10;
        double resources = health + otherRes * 4;
        double productivity = 1;
        double demand = 1;
        double minimumLifeExpectancy = 20;
        double maximumLifeExpectancy = 80;

        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "resources: " + resources,
                "health:",
                "  productivity: " + productivity,
                "  demand: " + demand,
                "  minimumLifeExpectancy: " + minimumLifeExpectancy,
                "  maximumLifeExpectancy: " + maximumLifeExpectancy
        ));
        int doctors = 100;
        int others = 10;
        int population = doctors + 4 * others;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status = Status.create(
                others, others, others, doctors, others,
                otherRes, otherRes, otherRes, health, otherRes,
                technology
        );

        double timeInterval = 1;
        double kh = eff * min(doctors * productivity, health) / demand / population;
        double lifeExpectancy = (maximumLifeExpectancy - minimumLifeExpectancy) * min(kh, 1) + minimumLifeExpectancy;
        double lambda = population * timeInterval / lifeExpectancy;

        ExtRandom random = mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = RulesSerde.loadHealthRule(jsonNode, random);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> edu = rule.apply(status, timeInterval);

        // Then ...
        assertEquals(Status.population(-3), edu._1);
        verify(random).nextPoisson(MockitoHamcrest.doubleThat(closeTo(lambda, 1e-6)));
    }

    @Test
    void loadOverSettlementRule() throws IOException {
        // Given ...
        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "resources: 100",
                "overSettlement:",
                "  preferredPopulationDensity: 0.9",
                "  deathTimeConstant: 2"
        ));

        ExtRandom random = mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3);

        int others = 25;
        double otherRes = 20;
        Status status = Status.create(
                others, others, others, others, others,
                otherRes, otherRes, otherRes, otherRes, otherRes,
                1);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = RulesSerde.loadOverSettlementRule(jsonNode, random);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> deaths = rule.apply(status, 1d);

        // Then ...
        assertEquals(Status.population(-3), deaths._1);
        verify(random).nextPoisson(anyDouble());
    }

    @Test
    void loadResearchRule() throws IOException {
        // Given ...
        double research = 100;
        double otherRes = 10;
        double resources = research + otherRes * 4;
        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "resources: " + resources,
                "research:",
                "  productivity: 1",
                "  cost: 1",
                "  timeConstant: 1",
                "  quantum: 0.01"
        ));
        int researchers = 100;
        int others = 10;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status = Status.create(
                others, researchers, others, others, others,
                otherRes, research, otherRes, otherRes, otherRes,
                technology
        );

        ExtRandom random = mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = RulesSerde.loadResearchRule(jsonNode, random);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> edu = rule.apply(status, 1d);

        // Then ...
        assertThat(edu._1.getTechnology(), closeTo(3 * 0.01, 1e-6));
        verify(random).nextPoisson(50);
    }
}