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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

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
    public static BiFunction<Status, Double, Status> educationRule(ExtRandom random, double resources, double productivity, double demand, double timeConstant) {
        double educatedByIndividual = productivity / timeConstant / demand;
        double educatedByResource = resources / timeConstant / demand;
        return (status, dt) -> {
            int population = status.getPopulation();
            double educators = status.getEducators();
            double ke = status.getEducationRatio();
            double efficiency = status.getEfficiency();
            double technology = status.getTechnology();
            double educatedPop = educators * educatedByIndividual;
            double educatedRes = ke * educatedByResource;
            double popByTime = population / timeConstant;
            double educatedByTime = efficiency * min(educatedPop, educatedRes);
            double lambda = max(0, (popByTime - educatedByTime)) * dt;
            int ne = lambda > 0 ? random.nextPoisson(lambda) : 0;
            double deltaT = -technology * min((double) ne / population, 1);
            logger.atDebug().log("educationRule: lambda={} popByTime={} educatedByTime={} educatedPop={} educatedRes={}",
                    lambda,
                    popByTime,
                    educatedByTime,
                    educatedPop,
                    educatedRes);
            if (ne > 0) {
                logger.atInfo().log("Technology loss {}", deltaT);
            }
            return Status.technology(deltaT);
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
    public static BiFunction<Status, Double, Status> foodProductionRule(ExtRandom random, double resources, double productivity, double demand, double deathTimeConstant, double birthTimeConstant) {
        double farmersDeaths = productivity / demand / deathTimeConstant;
        double foodDeaths = resources / demand / deathTimeConstant;

        double farmersBirths = productivity / demand / birthTimeConstant;
        double foodBirths = resources / demand / birthTimeConstant;

        return (status, dt) -> {
            double eta = status.getEfficiency();
            double farmers = status.getFarmers();
            int pop = status.getPopulation();
            double foodRatio = status.getFoodRatio();
            double maxByIndividuals = farmers * farmersDeaths;
            double maxByResources = foodRatio * foodDeaths;
            double popByDeathTime = pop / deathTimeConstant;
            double maxPopByDeathTime = eta * min(maxByIndividuals, maxByResources);
            double lambdaDeaths = max(
                    popByDeathTime - maxPopByDeathTime,
                    0) * dt;
            int deaths = lambdaDeaths > 0 ? random.nextPoisson(lambdaDeaths) : 0;
            logger.atDebug().log("foodProductionRule: lambdaDeaths={} popByDeathTime={} maxPopByDeathTime={} maxByIndividuals={} maxByResources={}",
                    lambdaDeaths,
                    popByDeathTime,
                    maxPopByDeathTime,
                    maxByIndividuals,
                    maxByResources
            );

            double maxBirthByIndividual = farmers * farmersBirths;
            double maxBirthByresource = foodRatio * foodBirths;
            double maxPopByBirthTime = eta * min(maxBirthByIndividual, maxBirthByresource);
            double popByBirthTime = pop / birthTimeConstant;
            double lambdaBirths = max(maxPopByBirthTime - popByBirthTime,
                    0) * dt;

            int births = lambdaBirths > 0 ? random.nextPoisson(lambdaBirths) : 0;
            logger.atDebug().log("foodProductionRule: lambdaBirth={} popByBirthTime={} maxPopByBirthTime={} maxBirthByIndividual={} maxBirthByresource={}",
                    lambdaBirths,
                    popByBirthTime,
                    maxPopByBirthTime,
                    maxBirthByIndividual,
                    maxBirthByresource
            );

            if (deaths > 0) {
                logger.atInfo().log("{} deaths from starvation", deaths);
            } else if (births > 0) {
                logger.atInfo().log("{} births", births);
            }
            return Status.population(births - deaths);
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
    public static BiFunction<Status, Double, Status> overSettlement(ExtRandom random, double resources, double density, double deathTimeConstant) {
        double deathsOver = resources * density / deathTimeConstant;
        return (status, dt) -> {
            // Computes the over settlement deaths
            int population = status.getPopulation();
            double settlementRatio = status.getSettlementRatio();
            double popByTime = population / deathTimeConstant;
            double maxPopByTime = settlementRatio * deathsOver;
            double lambdaOver = max(0, popByTime - maxPopByTime) * dt;
            logger.atDebug().log("overSettlement: lambda={} popByTime={} maxPopByTime={} lambdaOver={}",
                    lambdaOver,
                    popByTime,
                    maxPopByTime,
                    lambdaOver);
            int deaths = lambdaOver > 0 ? random.nextPoisson(lambdaOver) : 0;
            if (deaths > 0) {
                logger.atInfo().log(
                        "{} deaths for over settlement",
                        deaths);
            }
            return Status.population(-deaths);
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
    public static BiFunction<Status, Double, Status> researchRule(ExtRandom random, double resources, double productivity, double cost, double quantum) {
        double researcherSteps = productivity / cost;
        double researchSteps = resources / cost;
        return (status, dt) -> {
            double researchers = status.getResearchers();
            double researchRatio = status.getResearchRatio();
            double eta = status.getEfficiency();
            double popStep = researchers * researcherSteps;
            double resStep = researchRatio * researchSteps;
            double lambda = eta * min(popStep, resStep) * dt;
            int steps = lambda > 0 ? random.nextPoisson(lambda) : 0;
            logger.atDebug().log("researchRule: lambda={} popStep={} resStep={}",
                    lambda,
                    popStep,
                    resStep);
            if (steps == 0) {
                return Status.zero();
            }
            double deltaT = steps * quantum;
            logger.atInfo().log("Technology enhancement {}", deltaT);
            return Status.technology(deltaT);
        };
    }
}
