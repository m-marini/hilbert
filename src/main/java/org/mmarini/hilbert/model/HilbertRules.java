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

import org.mmarini.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Creates the hilbert rules
 */
public class HilbertRules {
    private static final Logger logger = LoggerFactory.getLogger(HilbertRules.class);

    /**
     * Returns the education rule
     *
     * @param random       the random number generator
     * @param resources    the total resources
     * @param productivity productivity by individual by unit time
     * @param demand       education demand by individual by unit time
     * @param timeConstant education ratio
     */
    public static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> educationRule(ExtRandom random, double resources, double productivity, double demand, double timeConstant) {
        return (status, dt) -> {

            int population = status.getPopulation();
            double educators = status.getEducators();
            double educationRatio = status.getEducationRatio();
            double efficiency = status.getEfficiency();
            double technology = status.getTechnology();

            double ke = efficiency * min(educators * productivity / population, educationRatio * resources) / demand;
            double lambda = max(0, (1 - ke)) * population * dt / timeConstant;
            int ne = lambda > 0 ? random.nextPoisson(lambda) : 0;
            double deltaT = -technology * min((double) ne / population, 1);
            logger.atDebug().log("educationRule: lambda={} ke={}",
                    lambda,
                    ke);
            if (ne > 0) {
                logger.atInfo().log("Technology loss {}", deltaT);
            }
            Supplier<Collection<Tuple2<String, Number>>> kpi = () -> List.of(
                    Tuple2.of("educationDeltaT", deltaT),
                    Tuple2.of("educationLambda", lambda),
                    Tuple2.of("educationKe", ke)
            );
            return Tuple2.of(Status.technology(deltaT), kpi);
        };
    }

    /**
     * Returns the food production rule
     *
     * @param random            the random number generator
     * @param resources         the total amount of resources
     * @param productivity      productivity by individual by unit time
     * @param demand            food demand by individual by unit time
     * @param deathTimeConstant death ratio from starvation
     * @param birthTimeConstant the birth ratio
     */
    public static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> foodProductionRule(ExtRandom random, double resources, double productivity, double demand, double deathTimeConstant, double birthTimeConstant) {
        return (status, dt) -> {
            double eta = status.getEfficiency();
            double farmers = status.getFarmers();
            int pop = status.getPopulation();
            double foodRatio = status.getFoodRatio();

            double kf = eta * min(productivity * farmers / pop, foodRatio * resources) / demand;
            double lambdaDeaths = max(0, pop * (1 - kf)) * dt / deathTimeConstant;
            int deaths = lambdaDeaths > 0 ? random.nextPoisson(lambdaDeaths) : 0;
            logger.atDebug().log("foodProductionRule: lambdaDeaths={} kf={}",
                    lambdaDeaths,
                    kf
            );

            double lambdaBirths = max(0, pop * (kf - 1)) * dt / birthTimeConstant;
            int births = lambdaBirths > 0 ? random.nextPoisson(lambdaBirths) : 0;
            logger.atDebug().log("foodProductionRule: lambdaBirth={}",
                    lambdaBirths
            );

            if (deaths > 0) {
                logger.atInfo().log("{} deaths from starvation", deaths);
            } else if (births > 0) {
                logger.atInfo().log("{} births", births);
            }
            Supplier<Collection<Tuple2<String, Number>>> builder = () -> List.of(
                    Tuple2.of("foodDeaths", -deaths),
                    Tuple2.of("foodBirths", births),
                    Tuple2.of("foodKf", kf),
                    Tuple2.of("foodLambdaDeaths", lambdaDeaths),
                    Tuple2.of("foodLambdaBirths", lambdaBirths)
            );
            return Tuple2.of(Status.population(births - deaths), builder);
        };
    }

    /**
     * Returns the over settlement rule
     * It generates random over settlement status changes
     * The over settlement happens when population exceeds the preferred density by settlement resource
     *
     * @param random            the random number generator
     * @param resources         the total resources
     * @param density           the preferred population density by settlement resources
     * @param deathTimeConstant the deaths time constant
     */
    public static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> overSettlement(ExtRandom random, double resources, double density, double deathTimeConstant) {
        double deathsOver = resources * density / deathTimeConstant;
        return (status, dt) -> {
            // Computes the over settlement deaths
            int population = status.getPopulation();
            double settlementRatio = status.getSettlementRatio();
            double maxPop = settlementRatio * deathsOver * dt;
            double pop = population / deathTimeConstant * dt;
            double lambda = max(0, pop - maxPop);
            logger.atDebug().log("overSettlement: lambda={}  maxPop={} pop={}",
                    lambda,
                    maxPop,
                    pop);
            int deaths = lambda > 0 ? random.nextPoisson(lambda) : 0;
            if (deaths > 0) {
                logger.atInfo().log(
                        "{} deaths for over settlement",
                        deaths);
            }
            Supplier<Collection<Tuple2<String, Number>>> kpi = () -> List.of(
                    Tuple2.of("overSettlementDeaths", -deaths),
                    Tuple2.of("overSettlementMaxPop", maxPop),
                    Tuple2.of("overSettlementPop", pop),
                    Tuple2.of("overSettlementLambda", lambda)
            );
            return Tuple2.of(Status.population(-deaths), kpi);
        };
    }

    /**
     * Returns the research rule
     *
     * @param random       the random number generator
     * @param resources    the total resources
     * @param productivity productivity by individual by unit time
     * @param cost         cost of quantum
     * @param quantum      the technology quantum step
     */
    public static BiFunction<Status, Double, Tuple2<Status, Supplier<Collection<Tuple2<String, Number>>>>> researchRule(ExtRandom random, double resources, double productivity, double cost, double quantum) {
        return (status, dt) -> {
            double researchers = status.getResearchers();
            double researchRatio = status.getResearchRatio();
            double eta = status.getEfficiency();

            double researchByPop = researchers * productivity;
            double researchByRes = researchRatio * resources;
            double lambda = eta * min(researchByPop, researchByRes) * dt / cost;
            int steps = lambda > 0 ? random.nextPoisson(lambda) : 0;
            logger.atDebug().log("researchRule: lambda={} popStep={} resStep={}",
                    lambda,
                    researchByPop,
                    researchByRes);
            double deltaT = steps * quantum;
            Status newStatus = steps == 0
                    ? Status.zero()
                    : Status.technology(deltaT);
            logger.atInfo().log("Technology enhancement {}", deltaT);
            Supplier<Collection<Tuple2<String, Number>>> builder = () -> List.of(
                    Tuple2.of("researchDeltaT", deltaT),
                    Tuple2.of("researchLambda", lambda)
            );
            return Tuple2.of(newStatus, builder);
        };
    }
}
