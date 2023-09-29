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
import org.mmarini.hilbert.TestFunctions;
import org.mmarini.yaml.Utils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusSerdeTest {

    public static final String TEST_YML = "./test.yml";

    @Test
    void loadStatus() throws IOException {
        // Given ...
        JsonNode jsonNode = Utils.fromText(TestFunctions.text(
                "---",
                "version: \"0.1\"",
                "population: 100",
                "farmerPrefs: -3",
                "researcherPrefs: -1",
                "educatorPrefs: 1",
                "inactivePrefs: 3",
                "foodPrefs: -3",
                "researchPrefs: -1",
                "educationPrefs: 1",
                "settlementPrefs: 3",
                "technology: 0.01"
        ));
        // When
        Status status = StatusSerde.fromJson(jsonNode);

        // Then ...
        assertEquals(100, status.getPopulation());
        assertEquals(-3, status.getFarmerPrefs());
        assertEquals(-1, status.getResearcherPrefs());
        assertEquals(1, status.getEducatorPrefs());
        assertEquals(3, status.getInactivePrefs());
        assertEquals(-3, status.getFoodPrefs());
        assertEquals(-1, status.getResearchPrefs());
        assertEquals(1, status.getEducationPrefs());
        assertEquals(3, status.getSettlementPrefs());
        assertEquals(0.01, status.getTechnology());
    }

    @Test
    void saveStatus() throws IOException {
        // Given ...
        int farmers = 1;
        int researchers = 2;
        int educators = 3;
        int inactive = 4;
        double food = 5;
        double research = 6;
        double education = 7;
        double settlement = 8;
        double technology = 0.5;
        Status status0 = Status.create(farmers, researchers, educators, inactive, food, research, education, settlement, technology);

        // When ...
        StatusSerde.write(new File(TEST_YML), status0);
        Status status1 = StatusSerde.fromFile(TEST_YML);

        // Then ...
        assertEquals(status0, status1);
    }

}