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

import org.mmarini.LazyValue;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.DoubleStream;

import static java.lang.Math.expm1;
import static java.lang.Math.max;
import static java.lang.String.format;
import static org.mmarini.hilbert.model.ExtMath.invSoftmax;
import static org.mmarini.hilbert.model.ExtMath.softmax;

/**
 * Represents the society status with the population,
 * the population distribution preferences (softmax)
 * and the resources' distribution preferences (softmax)
 */
public class Status {
    public static final Status ZERO = new Status(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    /**
     * Returns the status
     *
     * @param farmers     the number of farmers
     * @param researchers the number of researchers
     * @param educators   the number of educators
     * @param inactive    the number of inactive
     * @param food        the food resources
     * @param research    the research resources
     * @param education   the education resources
     * @param settlement  the settlement resources
     * @param technology  the technology level
     */
    public static Status create(int farmers, int researchers, int educators, int inactive,
                                double food, double research, double education, double settlement,
                                double technology) {
        if (farmers <= 0) {
            throw new IllegalArgumentException(format("Farmers must be positive (%d)", farmers));
        }
        if (researchers <= 0) {
            throw new IllegalArgumentException(format("Researchers must be positive (%d)", researchers));
        }
        if (educators <= 0) {
            throw new IllegalArgumentException(format("Educators must be positive (%d)", educators));
        }
        if (inactive <= 0) {
            throw new IllegalArgumentException(format("Inactive individuals must be positive (%d)", inactive));
        }
        if (food <= 0) {
            throw new IllegalArgumentException(format("Food must be positive (%e)", food));
        }
        if (research <= 0) {
            throw new IllegalArgumentException(format("Research must be positive (%e)", research));
        }
        if (education <= 0) {
            throw new IllegalArgumentException(format("Education must be positive (%e)", education));
        }
        if (settlement <= 0) {
            throw new IllegalArgumentException(format("Settlement must be positive (%e)", settlement));
        }
        int population = farmers + researchers + educators + inactive;
        double[] popPrefs = invSoftmax(farmers, researchers, educators, inactive);
        double[] resPrefs = invSoftmax(food, research, education, settlement);
        return new Status(population, popPrefs[0], popPrefs[1], popPrefs[2], popPrefs[3],
                resPrefs[0], resPrefs[1], resPrefs[2], resPrefs[3],
                technology);
    }

    /**
     * Returns the population change
     *
     * @param population the number of change individuals
     */
    public static Status population(int population) {
        return new Status(population, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Returns the sum of states
     *
     * @param states the states
     */
    public static Status sum(Status... states) {
        int population = 0;
        double farmerPrefs = 0;
        double researcherPrefs = 0;
        double educatorPrefs = 0;
        double inactivePrefs = 0;
        double foodPrefs = 0;
        double researchPrefs = 0;
        double educationPrefs = 0;
        double settlementPrefs = 0;
        double technology = 0;
        for (Status state : states) {
            population += state.population;
            farmerPrefs += state.farmerPrefs;
            researcherPrefs += state.researcherPrefs;
            educatorPrefs += state.educatorPrefs;
            inactivePrefs += state.inactivePrefs;
            foodPrefs += state.foodPrefs;
            researchPrefs += state.researchPrefs;
            educationPrefs += state.educationPrefs;
            settlementPrefs += state.settlementPrefs;
            technology += state.technology;
        }
        return new Status(population, farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs,
                foodPrefs, researchPrefs, educationPrefs, settlementPrefs, technology);
    }

    /**
     * Returns the technology only status
     *
     * @param value the technology value
     */
    public static Status technology(double value) {
        return new Status(0, 0, 0, 0, 0, 0, 0, 0, 0, value);
    }

    /**
     * Returns the no changes of status
     */
    public static Status zero() {
        return ZERO;
    }

    private final int population;
    private final double farmerPrefs;
    private final double researcherPrefs;
    private final double educatorPrefs;
    private final double inactivePrefs;
    private final double foodPrefs;
    private final double researchPrefs;
    private final double educationPrefs;
    private final double settlementPrefs;
    private final double technology;
    private final LazyValue<double[]> individuals;
    private final LazyValue<double[]> betas;

    /**
     * Creates the status
     *
     * @param population      the number of individual
     * @param farmerPrefs     the farmer preferences
     * @param researcherPrefs the researcher preferences
     * @param educatorPrefs   the educator preferences
     * @param inactivePrefs   the inactive preferences
     * @param foodPrefs       the food preferences
     * @param researchPrefs   the research preferences
     * @param educationPrefs  the education preferences
     * @param settlementPrefs the settlement preferences
     * @param technology      the technology level
     */
    public Status(int population, double farmerPrefs, double researcherPrefs, double educatorPrefs, double inactivePrefs,
                  double foodPrefs, double researchPrefs, double educationPrefs, double settlementPrefs, double technology) {
        this.population = population;
        this.farmerPrefs = farmerPrefs;
        this.researcherPrefs = researcherPrefs;
        this.educatorPrefs = educatorPrefs;
        this.inactivePrefs = inactivePrefs;
        this.foodPrefs = foodPrefs;
        this.researchPrefs = researchPrefs;
        this.educationPrefs = educationPrefs;
        this.settlementPrefs = settlementPrefs;
        this.technology = technology;
        this.individuals = new LazyValue<>(() ->
                Arrays.stream(softmax(farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs))
                        .limit(3)
                        .map(a -> a * population)
                        .toArray());
        this.betas = new LazyValue<>(() ->
                Arrays.stream(softmax(foodPrefs, researchPrefs, educationPrefs, settlementPrefs))
                        .limit(3)
                        .toArray()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return population == status.population && Double.compare(status.farmerPrefs, farmerPrefs) == 0 && Double.compare(status.researcherPrefs, researcherPrefs) == 0 && Double.compare(status.educatorPrefs, educatorPrefs) == 0 && Double.compare(status.inactivePrefs, inactivePrefs) == 0 && Double.compare(status.foodPrefs, foodPrefs) == 0 && Double.compare(status.researchPrefs, researchPrefs) == 0 && Double.compare(status.educationPrefs, educationPrefs) == 0 && Double.compare(status.settlementPrefs, settlementPrefs) == 0 && Double.compare(status.technology, technology) == 0;
    }

    public double getEducationPrefs() {
        return educationPrefs;
    }

    /**
     * Returns the research ratio
     */
    public double getEducationRatio() {
        return betas.get()[2];
    }

    public double getEducatorPrefs() {
        return educatorPrefs;
    }

    /**
     * Returns the number of farmers
     */
    public double getEducators() {
        return individuals.get()[2];
    }

    /**
     * Returns the processes' efficiency due to technology
     */
    double getEfficiency() {
        return -expm1(-technology);
    }

    public double getFarmerPrefs() {
        return farmerPrefs;
    }

    /**
     * Returns the number of farmers
     */
    public double getFarmers() {
        return individuals.get()[0];
    }

    public double getFoodPrefs() {
        return foodPrefs;
    }

    /**
     * Returns the food ratio
     */
    public double getFoodRatio() {
        return betas.get()[0];
    }

    public double getInactivePrefs() {
        return inactivePrefs;
    }

    public int getPopulation() {
        return population;
    }

    public double getResearchPrefs() {
        return researchPrefs;
    }

    /**
     * Returns the research ratio
     */
    public double getResearchRatio() {
        return betas.get()[1];
    }

    public double getResearcherPrefs() {
        return researcherPrefs;
    }

    /**
     * Returns the number of farmers
     */
    public double getResearchers() {
        return individuals.get()[1];
    }

    public double getSettlementPrefs() {
        return settlementPrefs;
    }

    /**
     * Returns the research ratio
     */
    public double getSettlementRatio() {
        return 1 - betas.get()[0] - betas.get()[1] - betas.get()[2];
    }

    public double getTechnology() {
        return technology;
    }

    /**
     * Returns the status with technology level
     *
     * @param technology the technology level
     */
    public Status setTechnology(double technology) {
        return technology != this.technology
                ? new Status(population, farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs,
                foodPrefs, researchPrefs, educationPrefs, settlementPrefs, technology)
                : this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(population, farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs, foodPrefs, researchPrefs, educationPrefs, settlementPrefs, technology);
    }

    /**
     * Returns the normalized status
     *
     * @param minTechnology the minimum level of technology
     */
    public Status normalize(double minTechnology) {
        double minPopPrefs = DoubleStream.of(farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs).min().orElseThrow();
        double maxPopPrefs = DoubleStream.of(farmerPrefs, researcherPrefs, educatorPrefs, inactivePrefs).max().orElseThrow();
        double minResPrefs = DoubleStream.of(foodPrefs, researchPrefs, educationPrefs, settlementPrefs).min().orElseThrow();
        double maxResPrefs = DoubleStream.of(foodPrefs, researchPrefs, educationPrefs, settlementPrefs).max().orElseThrow();
        double popRefsOffset = (maxPopPrefs + minPopPrefs) / 2;
        double resRefsOffset = (maxResPrefs + minResPrefs) / 2;
        int newPopulation = max(0, population);
        double newTechnology = max(minTechnology, technology);
        return newPopulation != population
                || newTechnology != technology
                || popRefsOffset != 0
                || resRefsOffset != 0
                ? new Status(newPopulation,
                farmerPrefs - popRefsOffset,
                researcherPrefs - popRefsOffset,
                educatorPrefs - popRefsOffset,
                inactivePrefs - popRefsOffset,
                foodPrefs - resRefsOffset,
                researchPrefs - resRefsOffset,
                educationPrefs - resRefsOffset,
                settlementPrefs - researchPrefs,
                newTechnology)
                : this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Status.class.getSimpleName() + "[", "]")
                .add("population=" + population)
                .add("farmerPrefs=" + farmerPrefs)
                .add("researcherPrefs=" + researcherPrefs)
                .add("educatorPrefs=" + educatorPrefs)
                .add("inactivePrefs=" + inactivePrefs)
                .add("foodPrefs=" + foodPrefs)
                .add("researchPrefs=" + researchPrefs)
                .add("educationPrefs=" + educationPrefs)
                .add("settlementPrefs=" + settlementPrefs)
                .add("technology=" + technology)
                .toString();
    }
}
