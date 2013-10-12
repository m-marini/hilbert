/**
 * 
 */
package org.mmarini.hilbert;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author US00852
 * 
 */
public class SocietySaxHandler extends DefaultHandler {
	private static final String SOCIETY = "society";

	private static final String FOOD_RESOURCE_PREFERENCE = "foodResourcePreference";
	private static final String RESEARCH_RESOURCE_PREFERENCE = "researchResourcePreference";
	private static final String EDUCATION_RESOURCE_PREFERENCE = "educationResourcePreference";
	private static final String SETTLEMENT_RESOURCE_PREFERENCE = "settlementResourcePreference";

	private static final String FOOD_POPULATION_PREFERENCE = "foodPopulationPreference";
	private static final String RESEARCH_POPULATION_PREFERENCE = "researchPopulationPreference";
	private static final String EDUCATION_POPULATION_PREFERENCE = "educationPopulationPreference";
	private static final String INACTIVE_POPULATION_PREFERENCE = "inactivePopulationPreference";

	private static final String TOTAL_RESOURCE = "totalResource";
	private static final String POPULATION = "population";

	private static final String PREFERED_POPULATION_DENSITY = "preferedPopulationDensity";
	private static final String RESOURCE_DEATH_RATE = "resourceDeathRate";

	private static final String INDIVIDUAL_FOOD_REQUEST = "individualFoodRequest";
	private static final String MINIMUM_RESOURCE_FOOD_PRODUCTIVITY = "minimumResourceFoodProductivity";
	private static final String MAXIMUM_RESOURCE_FOOD_PRODUCTIVITY = "maximumResourceFoodProductivity";
	private static final String BASELINE_RESOURCE_FOOD_PRODUCTIVITY = "baselineResourceFoodProductivity";
	private static final String FOOD_PRODUCTIVITY_REFERENCE = "foodProductivityReference";
	private static final String FOOD_PRODUCTIVITY_SIGMA_K = "foodProductivitySigmaK";
	private static final String AVG_FOOD_PRODUCTIVITY_CYCLE_INTERVAL = "avgFoodProductivityCycleInterval";
	private static final String MINIMUM_INDIVIDUAL_FOOD_PRODUCTIVITY = "minimumIndividualFoodProductivity";
	private static final String INDIVIDUAL_FOOD_PRODUCTIVITY_RATE = "individualFoodProductivityRate";
	private static final String FAMINE_DEATH_RATE = "famineDeathRate";
	private static final String BIRTH_RATE = "birthRate";

	private static final String RESEARCH_REFERENCE = "researchReference";
	private static final String MINIMUM_RESOURCE_RESEARCH_PRODUCTIVITY = "minimumResourceResearchProductivity";
	private static final String RESOURCE_RESEARCH_PRODUCTIVITY_RATE = "resourceResearchProductivityRate";
	private static final String MINIMUM_INDIVIDUAL_RESEARCH_PRODUCTIVITY = "minimumIndividualResearchProductivity";
	private static final String INDIVIDUAL_RESEARCH_PRODUCTIVITY_RATE = "individualResearchProductivityRate";

	private static final String EDUCATION_REFERENCE = "educationReference";
	private static final String MINIMUM_RESOURCE_EDUCATION_PRODUCTIVITY = "minimumResourceEducationProductivity";
	private static final String RESOURCE_EDUCATION_PRODUCTIVITY_RATE = "resourceEducationProductivityRate";
	private static final String MINIMUM_INDIVIDUAL_EDUCATION_PRODUCTIVITY = "minimumIndividualEducationProductivity";
	private static final String INDIVIDUAL_EDUCATION_PRODUCTIVITY_RATE = "individualEducationProductivityRate";

	private StringBuilder text;
	private Society society;
	private Locator locator;

	/**
	 * 
	 */
	public SocietySaxHandler() {
		text = new StringBuilder();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		text.append(ch, start, length);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		switch (localName) {
		case FOOD_RESOURCE_PREFERENCE:
			society.setFoodResourcePreference(parseDouble());
			break;
		case RESEARCH_RESOURCE_PREFERENCE:
			society.setResearchResourcePreference(parseDouble());
			break;
		case EDUCATION_RESOURCE_PREFERENCE:
			society.setEducationResourcePreference(parseDouble());
			break;
		case SETTLEMENT_RESOURCE_PREFERENCE:
			society.setSettlementResourcePreference(parseDouble());
			break;
		case FOOD_POPULATION_PREFERENCE:
			society.setFoodPopulationPreference(parseDouble());
			break;
		case RESEARCH_POPULATION_PREFERENCE:
			society.setResearchPopulationPreference(parseDouble());
			break;
		case EDUCATION_POPULATION_PREFERENCE:
			society.setEducationPopulationPreference(parseDouble());
			break;
		case INACTIVE_POPULATION_PREFERENCE:
			society.setInactivePopulationPreference(parseDouble());
			break;
		case TOTAL_RESOURCE:
			society.setTotalResource(parseDouble());
			break;
		case POPULATION:
			society.setPopulation(parseInt());
			break;
		case PREFERED_POPULATION_DENSITY:
			society.setPreferedPopulationDensity(parseDouble());
			break;
		case RESOURCE_DEATH_RATE:
			society.setResourceDeathRate(parseDouble());
			break;
		case INDIVIDUAL_FOOD_REQUEST:
			society.setIndividualFoodRequest(parseDouble());
			break;
		case MINIMUM_RESOURCE_FOOD_PRODUCTIVITY:
			society.setMinimumResourceFoodProductivity(parseDouble());
			break;
		case MAXIMUM_RESOURCE_FOOD_PRODUCTIVITY:
			society.setMaximumResourceFoodProductivity(parseDouble());
			break;
		case BASELINE_RESOURCE_FOOD_PRODUCTIVITY:
			society.setBaselineResourceFoodProductivity(parseDouble());
			break;
		case FOOD_PRODUCTIVITY_REFERENCE:
			society.setFoodProductivityReference(parseDouble());
			break;
		case FOOD_PRODUCTIVITY_SIGMA_K:
			society.setFoodProductivitySigmaK(parseDouble());
			break;
		case AVG_FOOD_PRODUCTIVITY_CYCLE_INTERVAL:
			society.setAvgFoodProductivityCycleInterval(parseDouble());
			break;
		case MINIMUM_INDIVIDUAL_FOOD_PRODUCTIVITY:
			society.setMinimumIndividualFoodProductivity(parseDouble());
			break;
		case INDIVIDUAL_FOOD_PRODUCTIVITY_RATE:
			society.setIndividualFoodProductivityRate(parseDouble());
			break;
		case FAMINE_DEATH_RATE:
			society.setFamineDeathRate(parseDouble());
			break;
		case BIRTH_RATE:
			society.setBirthRate(parseDouble());
			break;
		case RESEARCH_REFERENCE:
			society.setResearchReference(parseDouble());
			break;
		case MINIMUM_RESOURCE_RESEARCH_PRODUCTIVITY:
			society.setMinimumResourceResearchProductivity(parseDouble());
			break;
		case RESOURCE_RESEARCH_PRODUCTIVITY_RATE:
			society.setResourceResearchProductivityRate(parseDouble());
			break;
		case MINIMUM_INDIVIDUAL_RESEARCH_PRODUCTIVITY:
			society.setMinimumIndividualResearchProductivity(parseDouble());
			break;
		case INDIVIDUAL_RESEARCH_PRODUCTIVITY_RATE:
			society.setIndividualResearchProductivityRate(parseDouble());
			break;
		case EDUCATION_REFERENCE:
			society.setEducationReference(parseDouble());
			break;
		case MINIMUM_RESOURCE_EDUCATION_PRODUCTIVITY:
			society.setMinimumResourceEducationProductivity(parseDouble());
			break;
		case RESOURCE_EDUCATION_PRODUCTIVITY_RATE:
			society.setResourceEducationProductivityRate(parseDouble());
			break;
		case MINIMUM_INDIVIDUAL_EDUCATION_PRODUCTIVITY:
			society.setMinimumResourceEducationProductivity(parseDouble());
			break;
		case INDIVIDUAL_EDUCATION_PRODUCTIVITY_RATE:
			society.setIndividualEducationProductivityRate(parseDouble());
			break;
		case SOCIETY:
			break;

		default:
			throw new SAXParseException("Element \"" + localName
					+ "\" not expected", locator);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		throw e;
	}

	/**
	 * 
	 * @return
	 */
	private double parseDouble() {
		return Double.parseDouble(text.toString());
	}

	/**
	 * 
	 * @return
	 */
	private int parseInt() {
		return Integer.parseInt(text.toString());
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#setDocumentLocator(org.xml.sax.Locator
	 *      )
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/**
	 * 
	 * @param society
	 */
	public void setSociety(Society society) {
		this.society = society;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		text.setLength(0);
	}

}
