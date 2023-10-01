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

import org.junit.jupiter.api.Test;
import org.mmarini.Matchers;
import org.mmarini.Tuple2;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.Math.log;
import static java.lang.Math.min;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mmarini.Matchers.tupleOf;
import static org.mockito.Mockito.*;

class HilbertRulesTest {

    @Test
    void educationRuleOverEducation() {
        int educators = 100;
        int others = 10;
        int population = educators + 3 * others;
        double education = 100;
        double otherRes = 10;
        double resources = education + 3 * otherRes;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status0 = Status.create(
                others, others, educators, others,
                otherRes, otherRes, education, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(200); // 3 dead's
        double ke = 2;
        // let the education production limited only by educator (epsilonp * educators < epsilonr * education)
        // ka = eff * min(pip * farmers, pis * food)/(pop*rho)
        double productivity = 1;
        double demand = eff * productivity * educators / population / ke; // eff * pip * farmers/pop/ka
        double timeConstant = 1;
        double timeInterval = 1;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.educationRule(random, resources, productivity, demand, timeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertThat(delta._1.getTechnology(), closeTo(0, 1e-6));
        verify(random, never()).nextPoisson(anyDouble());

        assertThat(kpi, containsInAnyOrder(
                tupleOf("deltaTE", -0d),
                tupleOf("lambdaE", 0d),
                tupleOf("ke", ke)));
    }

    @Test
    void educationRuleUnderEducation() {
        int educators = 100;
        int others = 10;
        int population = educators + 3 * others;
        double education = 100;
        double otherRes = 10;
        double resources = education + 3 * otherRes;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status0 = Status.create(
                others, others, educators, others,
                otherRes, otherRes, education, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double ke = 0.5;
        // let the education production limited only by educator (epsilonp * educators < epsilonr * education)
        // ka = eff * min(pip * farmers, pis * food)/(pop*rho)
        double productivity = 1;
        double demand = eff * productivity * educators / population / ke; // eff * pip * farmers/pop/ka
        double timeConstant = 1;
        double timeInterval = 1;

        double lambda = ke * population;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.educationRule(random, resources, productivity, demand, timeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertThat(delta._1.getTechnology(), closeTo(-technology * 3 / population, 1e-6));
        verify(random).nextPoisson(MockitoHamcrest.doubleThat(closeTo(lambda, 1e-6)));

        assertThat(kpi, containsInAnyOrder(
                tupleOf(equalTo("deltaTE"), closeTo(-technology * 3 / population, 1e-6)),
                tupleOf(equalTo("lambdaE"), closeTo(lambda, 1e-6)),
                tupleOf(equalTo("ke"), closeTo(ke, 1e-6))
        ));
    }

    @Test
    void foodProductionRuleBirth() {
        int farmers = 70;
        int others = 10;
        int population = farmers + 3 * others;
        double food = 100;
        double otherRes = 10;
        double resources = food + 3 * otherRes;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status0 = Status.create(
                farmers, others, others, others,
                food, otherRes, otherRes, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double ka = 2;
        // let the food production limited only by farmers (pip * farmer < pis*food)
        // ka = eff * min(pip * farmers, pis * food)/(pop*rho)
        double productivity = 1;
        double demand = eff * productivity * farmers / population / ka; // eff * pip * farmers/pop/ka
        double birthTimeConstant = 1;
        double timeInterval = 1;
        double lambda = (eff * farmers * productivity / demand / birthTimeConstant - population / birthTimeConstant) * timeInterval;
        double kfPop = eff * farmers / population / demand;
        double kfRes = eff * food / demand / population;
        double kf = min(kfPop, kfRes);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.foodProductionRule(random, resources, productivity, demand, 1, birthTimeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertEquals(Status.population(3), delta._1);
        verify(random, only()).nextPoisson(lambda);
        assertThat(kpi, containsInAnyOrder(
                Matchers.<String, Number>tupleOf("deathsS", 0),
                Matchers.<String, Number>tupleOf("births", 3),
                tupleOf("kf", kf),
                tupleOf("kfPop", kfPop),
                tupleOf("kfRes", kfRes),
                tupleOf("lambdaS", 0d),
                tupleOf("lambdaB", lambda)
        ));
    }

    @Test
    void foodProductionRuleFamine() {
        int farmers = 70;
        int others = 10;
        int population = farmers + 3 * others;
        double food = 100;
        double otherRes = 10;
        double resources = food + 3 * otherRes;
        double eff = 0.5; // 1 - exp(-technology)

        double technology = -log(1 - eff); // -log(1-eff)
        Status status0 = Status.create(
                farmers, others, others, others,
                food, otherRes, otherRes, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double ka = 0.5;
        // let the food production limited only by farmers (pip * farmer < pis*food)
        // ka = eff * min(pip * farmers, pis * food)/(pop*rho)
        double productivity = 1;
        double demand = eff * productivity * farmers / population / ka; // eff * pip * farmers/pop/ka
        double deathTimeConstant = 1;
        double timeInterval = 1;
        double lambda = population / deathTimeConstant - eff * farmers * productivity / demand / deathTimeConstant * timeInterval;
        double kfPop = eff * productivity * farmers / population / demand;
        double kfRes = eff * food / population / demand;
        double kf = min(kfPop, kfRes);

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.foodProductionRule(random, resources, productivity, demand, deathTimeConstant, 1);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertEquals(Status.population(-3), delta._1);
        verify(random, only()).nextPoisson(lambda);
        assertThat(kpi, containsInAnyOrder(
                Matchers.<String, Number>tupleOf("deathsS", -3),
                Matchers.<String, Number>tupleOf("births", 0),
                Matchers.tupleOf("kf", kf),
                Matchers.tupleOf("kfPop", kfPop),
                Matchers.tupleOf("kfRes", kfRes),
                Matchers.tupleOf("lambdaS", lambda),
                Matchers.tupleOf("lambdaB", 0d)
        ));
    }

    @Test
    void noOverSettlement() {
        // Given
        // a population under the max density
        double settlement = 50;
        int otherRes = 10;
        Status status0 = Status.create(
                25, 25, 25, 25,
                otherRes, otherRes, otherRes, settlement,
                0);
        double population = 100;
        double deathTimeConstant = 1;
        double density = 2;
        double pop = population / deathTimeConstant;
        double maxPop = settlement * density / deathTimeConstant;
        double resources = otherRes * 3 + settlement;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.overSettlement(new ExtRandom(), resources, density, deathTimeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, 1d);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertEquals(Status.zero(), delta._1);

        assertThat(kpi, containsInAnyOrder(
                Matchers.<String, Number>tupleOf("deathsO", 0),
                tupleOf("maxPopO", maxPop),
                tupleOf("popO", pop),
                tupleOf("lambdaO", 0d)
        ));
    }

    @Test
    void overSettlement() {
        // Given
        // over settlement population by 10 individuals
        // and a death ratio of 0.5
        int population = 100;
        double settlement = 25;
        Status status0 = Status.create(
                25, 25, 25, 25,
                25, 25, 25, settlement,
                0);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double density = 0.9;
        double timeInterval = 1;
        double deathTimeConstant = 2;
        double lambda = population / deathTimeConstant - settlement * density / deathTimeConstant * timeInterval;
        double pop = population * timeInterval / deathTimeConstant;
        double maxPop = settlement * density * timeInterval / deathTimeConstant;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.overSettlement(random, 100, density, deathTimeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertEquals(Status.population(-3), delta._1);
        verify(random).nextPoisson(lambda);

        assertThat(kpi, containsInAnyOrder(
                Matchers.<String, Number>tupleOf("deathsO", -3),
                tupleOf("maxPopO", maxPop),
                tupleOf("popO", pop),
                tupleOf("lambdaO", lambda)
        ));
    }

    @Test
    void overSettlementNoDeath() {
        double settlement = 25;
        Status status0 = Status.create(
                25, 25, 25, 25,
                25, 25, 25, settlement,
                0);
        int population = 25 * 4;
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(0); // 0 dead's
        double density = 10;
        double deathTimeConstant = 2;
        double timeInterval = 1;
        double lambda = 0;
        double pop = population * timeInterval / deathTimeConstant;
        double maxPop = settlement * density * timeInterval / deathTimeConstant;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.overSettlement(random, 100, density, deathTimeConstant);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertEquals(Status.zero(), delta._1);
        verify(random, never()).nextPoisson(anyDouble());

        assertThat(kpi, containsInAnyOrder(
                Matchers.<String, Number>tupleOf("deathsO", 0),
                tupleOf("maxPopO", maxPop),
                tupleOf("popO", pop),
                tupleOf("lambdaO", lambda)
        ));
    }

    @Test
    void researchRuleByResearchers() {
        int researchers = 10;
        int others = 10;
        double research = 100;
        double otherRes = 10;
        double resources = research + 3 * otherRes;

        double eff = 0.5; // 1 - exp(-technology)
        double technology = -log(1 - eff); // -log(1-eff)

        Status status0 = Status.create(
                others, researchers, others, others,
                otherRes, research, otherRes, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double productivity = 1;
        double cost = 1;
        double quantum = 0.01;
        double timeInterval = 1;
        double lambda = eff * researchers * productivity / cost * timeInterval;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.researchRule(random, resources, productivity, cost, quantum);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertThat(delta._1.getTechnology(), closeTo(3 * quantum, 1e-6));
        verify(random).nextPoisson(lambda);

        assertThat(kpi, containsInAnyOrder(
                tupleOf("deltaTR", 3 * quantum),
                tupleOf("lambdaR", lambda)
        ));
    }

    @Test
    void researchRuleByResources() {
        int researchers = 100;
        int others = 100;
        double research = 10;
        double otherRes = 1;
        double resources = research + 3 * otherRes;

        double eff = 0.5; // 1 - exp(-technology)
        double technology = -log(1 - eff); // -log(1-eff)

        Status status0 = Status.create(
                others, researchers, others, others,
                otherRes, research, otherRes, otherRes,
                technology);
        ExtRandom random = Mockito.mock();
        when(random.nextPoisson(anyDouble())).thenReturn(3); // 3 dead's
        double productivity = 1;
        double cost = 1;
        double quantum = 0.01;
        double timeInterval = 1;
        double lambda = eff * research / cost * timeInterval;

        // When ...
        BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> rule = HilbertRules.researchRule(random, resources, productivity, cost, quantum);
        Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>> delta = rule.apply(status0, timeInterval);
        Collection<Tuple2<String, Number>> kpi = delta._2.get();

        // Then ...
        assertThat(delta._1.getTechnology(), closeTo(3 * quantum, 1e-6));
        verify(random).nextPoisson(lambda);

        assertThat(kpi, containsInAnyOrder(
                tupleOf("deltaTR", 3 * quantum),
                tupleOf("lambdaR", lambda)
        ));
    }
}