package org.mmarini.hilbert;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * 
 * @author US00852
 * 
 */
public class Society {

	private static final String SOCIETY_XSD = "/society-0-0-1.xsd";
	private static final String PARAMETERS_XML = "/society.xml";
	private static final String STEP_SEPARATOR = "-----------------------------------------";
	private static final String DEBUG_SEPARATOR = "##########################################";

	private static MessageFormatLog log = new MessageFormatLog(Society.class);

	private List<Rule> rules;
	private Random random;
	private int stepCount;
	private PrintWriter dataWriter;

	private double foodResourcePreference;
	private double researchResourcePreference;
	private double educationResourcePreference;
	private double settlementResourcePreference;

	private double foodPopulationPreference;
	private double researchPopulationPreference;
	private double educationPopulationPreference;
	private double inactivePopulationPreference;

	private double totalResource;
	private double researchResource;
	private double educationResource;
	private double foodResource;
	private double settlementResource;

	private int population;
	private int foodPopulation;
	private int researchPopulation;
	private int educationPopulation;

	private double totalPopulationPreference;

	private double preferedPopulationDensity;
	private double resourceDeathRate;

	private double individualFoodRequest;
	private double minimumResourceFoodProductivity;
	private double maximumResourceFoodProductivity;
	private double baselineResourceFoodProductivity;
	private double foodProductivityReference;
	private double foodProductivitySigmaK;
	private double avgFoodProductivityCycleInterval;
	private double minimumIndividualFoodProductivity;
	private double individualFoodProductivityRate;
	private double famineDeathRate;
	private double birthRate;

	private double researchReference;
	private double minimumResourceResearchProductivity;
	private double resourceResearchProductivityRate;
	private double minimumIndividualResearchProductivity;
	private double individualResearchProductivityRate;

	private double educationReference;
	private double minimumResourceEducationProductivity;
	private double resourceEducationProductivityRate;
	private double minimumIndividualEducationProductivity;
	private double individualEducationProductivityRate;

	private double preferedPopulation;
	private double tecnology;
	private double resourceFoodProductivity;
	private double foodProduction;
	private double foodRate;
	private boolean headerLogged;

	/**
	 * 
	 */
	public Society() {
		rules = new ArrayList<>();
		createRules();
	}

	/**
	 * 
	 */
	private void born() {
		int birthByFood = (int) Math.round(foodProduction
				/ individualFoodRequest - population);
		int birthByPopulation = (int) Math.round(population * birthRate);
		int maxBirths = Math.max(Math.min(birthByPopulation, birthByFood), 1);
		int births = random.nextInt(maxBirths + 1);
		if (births <= 0)
			return;
		population += births;

		logHeader();

		log.debug(DEBUG_SEPARATOR);
		log.debug("Max births by food = {0}", birthByFood);
		log.debug("Max births by population = {0}", birthByPopulation);
		log.debug("Max births = {0}", maxBirths);

		log.info(
				"Born {0} individuals, population to {1} individuals, resource food productivity is {2}",
				births, population, resourceFoodProductivity);
	}

	/**
	 * 
	 */
	private void changeResourceProductivity() {
		double rate = Math.exp(-tecnology / foodProductivityReference);
		double average = maximumResourceFoodProductivity
				- (maximumResourceFoodProductivity - minimumResourceFoodProductivity)
				* rate;
		double sigma = average * foodProductivitySigmaK;
		resourceFoodProductivity = random.nextGaussian() * sigma + average;
		if (resourceFoodProductivity < baselineResourceFoodProductivity)
			resourceFoodProductivity = baselineResourceFoodProductivity;

		log.debug(DEBUG_SEPARATOR);
		log.debug("Resource food production rate = {0}", rate);
		log.debug("Average resource productivity = {0}", average);
		log.debug("Sigma resource productivity = {0}", sigma);
		log.debug("New food production cycle productivity to {0}",
				resourceFoodProductivity);
	}

	/**
	 * 
	 */
	private void clearHeader() {
		headerLogged = false;
	}

	/**
	 * 
	 */
	private void computePreprocessInfo() {
		foodPopulation = (int) Math.round(population * foodPopulationPreference
				/ totalPopulationPreference);
		researchPopulation = (int) Math.round(population
				* researchPopulationPreference / totalPopulationPreference);
		educationPopulation = (int) Math.round(population
				* educationPopulationPreference / totalPopulationPreference);
		double individualFoodProductivity = minimumIndividualFoodProductivity
				+ individualFoodProductivityRate * tecnology;
		double individualFoodProduction = individualFoodProductivity
				* foodPopulation;
		double resourceFoodProduction = resourceFoodProductivity * foodResource;

		foodProduction = Math.min(individualFoodProduction,
				resourceFoodProduction);
		double foodRequest = population * individualFoodRequest;

		foodRate = foodProduction / foodRequest;

		clearHeader();
		log.debug(DEBUG_SEPARATOR);
		log.debug("Farmers = {0}", foodPopulation);
		log.debug("Researchers = {0}", researchPopulation);
		log.debug("Educators = {0}", educationPopulation);
		log.debug("Resource food productivity = {0}", resourceFoodProductivity);
		log.debug("Individual food productivity = {0}",
				individualFoodProductivity);

		log.debug("Resource food production = {0}", resourceFoodProduction);
		log.debug("Individual food production = {0}", individualFoodProduction);

		log.debug("Food request = {0}", foodRequest);
		log.debug("Food production = {0}", foodProduction);

		log.debug("Food rate = {0,number,percent}", foodRate);
	}

	/**
	 * Create the rules
	 */
	private void createRules() {
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				deadForOversettlement();
			}

			@Override
			public boolean isCauseMatched() {
				return isOverSettlement();
			}
		});
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				changeResourceProductivity();
			}

			@Override
			public boolean isCauseMatched() {
				return random.nextDouble() < 1. / avgFoodProductivityCycleInterval;
			}
		});
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				deadForFamine();
			}

			@Override
			public boolean isCauseMatched() {
				return isFamine();
			}
		});
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				born();
			}

			@Override
			public boolean isCauseMatched() {
				return isBorning();
			}
		});
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				discover();
			}

			@Override
			public boolean isCauseMatched() {
				return isDiscovering();
			}
		});
		rules.add(new Rule() {

			@Override
			public void applyEffect() {
				regret();
			}

			@Override
			public boolean isCauseMatched() {
				return isRegression();
			}
		});
	}

	/**
	 * 
	 */
	private void deadForFamine() {
		int maxDeaths = Math.min(
				(int) Math.round(2 * population * famineDeathRate), population);
		if (maxDeaths == 1)
			return;
		int deaths = random.nextInt(maxDeaths);
		if (deaths <= 0)
			return;
		population -= deaths;

		logHeader();

		log.debug(DEBUG_SEPARATOR);
		log.debug("Max famine death = {0}", maxDeaths);

		log.info(
				"Dead {0} individuals for famine, population to {1} individuals, resource food productivity is {2}",
				deaths, population, resourceFoodProductivity);
	}

	/**
	 * 
	 */
	private void deadForOversettlement() {
		int m = (int) Math.round(2. * population * resourceDeathRate);
		if (m <= 0)
			return;
		int n = random.nextInt(m);
		if (n <= 0)
			return;
		population -= n;

		logHeader();

		log.debug(DEBUG_SEPARATOR);
		log.debug("Oversettlement max deaths = {0}", m);

		log.info(
				"Dead {0} individuals for oversettlement, population to {1} individuals",
				n, population);
	}

	/**
	 * 
	 */
	private void discover() {
		++tecnology;
		logHeader();
		log.info("New discovery tecnology to {0}", tecnology);
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasPopulation() {
		return population > 0;
	}

	/**
	 * Initialize the society
	 * 
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void init() throws SAXException, IOException,
			ParserConfigurationException {
		loadParameters();
		double tot = foodResourcePreference + researchResourcePreference
				+ educationResourcePreference + settlementResourcePreference;
		foodResource = totalResource * foodResourcePreference / tot;
		researchResource = totalResource * researchResourcePreference / tot;
		educationResource = totalResource * educationResourcePreference / tot;
		settlementResource = totalResource - foodResource - researchResource
				- educationResource;
		totalPopulationPreference = foodPopulationPreference
				+ researchPopulationPreference + educationPopulationPreference
				+ inactivePopulationPreference;
		preferedPopulation = preferedPopulationDensity * settlementResource;
		changeResourceProductivity();
		dataWriter
				.println("Step Population Tecnology ResourceFoodProductivity FoodProduction FoodRate");
	}

	/**
	 * 
	 * @return
	 */
	private boolean isBorning() {
		if (foodRate < 1)
			return false;
		double prob = 1 - 1 / foodRate;
		log.debug(DEBUG_SEPARATOR);
		log.debug("Borning probability = {0,number,percent}", prob);
		return random.nextDouble() < prob;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isDiscovering() {
		double resourceResearchProductivity = minimumResourceResearchProductivity
				+ resourceResearchProductivityRate * tecnology;
		double individualResearchProductivity = minimumIndividualResearchProductivity
				+ individualResearchProductivityRate * tecnology;
		double resourceResearch = resourceResearchProductivity
				* researchResource;
		double populationResearch = individualResearchProductivity
				* researchPopulation;
		double research = Math.min(populationResearch, resourceResearch);
		double prob = -Math.expm1(-research / researchReference);

		log.debug(DEBUG_SEPARATOR);
		log.debug("Resource research productivity = {0}",
				resourceResearchProductivity);
		log.debug("Individual research productivity = {0}",
				individualResearchProductivity);
		log.debug("Resource research = {0}", resourceResearch);
		log.debug("Population research = {0}", populationResearch);
		log.debug("Research = {0}", research);
		log.debug("Discover probability = {0,number,percent}", prob);
		return random.nextDouble() < prob;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isFamine() {
		double prob = 1 - foodRate;
		log.debug(DEBUG_SEPARATOR);
		log.debug("Famine probability = {0,number,percent}", prob);
		if (prob <= 0.)
			return false;
		if (prob >= 1.)
			return true;
		return random.nextDouble() < prob;
	}

	/**
	 * 
	 * @return
	 */
	private boolean isOverSettlement() {
		double prob = population / preferedPopulation - 1;
		log.debug(DEBUG_SEPARATOR);
		log.debug("Oversettlment probability = {0,number,percent}", prob);
		if (prob <= 0.) {
			log.debug("No oversettlment");
			return false;
		}
		if (prob >= 1.) {
			return true;
		}
		boolean overSettlemnt = random.nextDouble() < prob;
		return overSettlemnt;
	}

	/**
	 * 
	 * @return
	 */
	protected boolean isRegression() {
		double resourceEducationProductivity = minimumResourceEducationProductivity
				+ resourceEducationProductivityRate * tecnology;
		double individualEducationProductivity = minimumIndividualEducationProductivity
				+ individualEducationProductivityRate * tecnology;
		double resourceEducation = resourceEducationProductivity
				* educationResource;
		double populationEducation = individualEducationProductivity
				* educationPopulation;
		double education = Math.min(populationEducation, resourceEducation);
		double prob = Math.exp(-education / (population * educationReference));

		log.debug(DEBUG_SEPARATOR);
		log.debug("Resource education productivity = {0}",
				resourceEducationProductivity);
		log.debug("Individual education productivity = {0}",
				individualEducationProductivity);
		log.debug("Resource education = {0}", resourceEducation);
		log.debug("Population education = {0}", populationEducation);
		log.debug("Education = {0}", education);
		log.debug("Regression probability = {0,number,percent}", prob);
		return random.nextDouble() < prob;
	}

	/**
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 */
	private void loadParameters() throws SAXException, IOException,
			ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		InputStream schemaInputStream = getClass().getResourceAsStream(
				SOCIETY_XSD);
		Source schemaSource = new StreamSource(schemaInputStream);
		Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource);
		factory.setSchema(schema);
		SAXParser parser = factory.newSAXParser();
		InputStream inputStream = getClass()
				.getResourceAsStream(PARAMETERS_XML);
		SocietySaxHandler handler = new SocietySaxHandler();
		handler.setSociety(this);
		parser.parse(inputStream, handler);
	}

	/**
	 * 
	 */
	private void logHeader() {
		if (!headerLogged) {
			log.info(STEP_SEPARATOR);
			log.info("Step {0,number,00000}", stepCount);
			headerLogged = true;
		}
	}

	/**
	 * @return
	 * 
	 */
	public void processStep() {
		computePreprocessInfo();
		log.debug(DEBUG_SEPARATOR);
		for (Rule rule : rules) {
			if (rule.isCauseMatched())
				rule.applyEffect();
		}
		++stepCount;
		dataWriter.print(stepCount);
		dataWriter.print(" ");
		dataWriter.print(population);
		dataWriter.print(" ");
		dataWriter.print(tecnology);
		dataWriter.print(" ");
		dataWriter.print(resourceFoodProductivity);
		dataWriter.print(" ");
		dataWriter.print(foodProduction);
		dataWriter.print(" ");
		dataWriter.print(foodRate);
		dataWriter.println();

		log.debug(DEBUG_SEPARATOR);
		log.debug("Step = " + population);
		log.debug("Population = " + population);
	}

	/**
	 * 
	 */
	protected void regret() {
		if (tecnology <= 0)
			return;
		--tecnology;
		logHeader();
		log.info("Regression tecnology to {0}", tecnology);
	}

	/**
	 * @param avgFoodProductivityCycleInterval
	 *            the avgFoodProductivityCycleInterval to set
	 */
	public void setAvgFoodProductivityCycleInterval(
			double avgFoodProductivityCycleInterval) {
		this.avgFoodProductivityCycleInterval = avgFoodProductivityCycleInterval;
	}

	/**
	 * @param baselineResourceFoodProductivity
	 *            the baselineResourceFoodProductivity to set
	 */
	public void setBaselineResourceFoodProductivity(
			double baselineResourceFoodProductivity) {
		this.baselineResourceFoodProductivity = baselineResourceFoodProductivity;
	}

	/**
	 * @param birthRate
	 *            the birthRate to set
	 */
	public void setBirthRate(double birthRate) {
		this.birthRate = birthRate;
	}

	/**
	 * 
	 * @param dataWriter
	 */
	public void setDataWriter(PrintWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	/**
	 * @param educationPopulationPreference
	 *            the educationPopulationPreference to set
	 */
	public void setEducationPopulationPreference(
			double educationPopulationPreference) {
		this.educationPopulationPreference = educationPopulationPreference;
	}

	/**
	 * @param educationReference
	 *            the educationReference to set
	 */
	public void setEducationReference(double educationReference) {
		this.educationReference = educationReference;
	}

	/**
	 * @param educationResourcePreference
	 *            the educationResourcePreference to set
	 */
	public void setEducationResourcePreference(
			double educationResourcePreference) {
		this.educationResourcePreference = educationResourcePreference;
	}

	/**
	 * @param famineDeathRate
	 *            the famineDeathRate to set
	 */
	public void setFamineDeathRate(double famineDeathRate) {
		this.famineDeathRate = famineDeathRate;
	}

	/**
	 * @param foodPopulationPreference
	 *            the foodPopulationPreference to set
	 */
	public void setFoodPopulationPreference(double foodPopulationPreference) {
		this.foodPopulationPreference = foodPopulationPreference;
	}

	/**
	 * @param foodProductivityReference
	 *            the foodProductivityReference to set
	 */
	public void setFoodProductivityReference(double foodProductivityReference) {
		this.foodProductivityReference = foodProductivityReference;
	}

	/**
	 * @param foodProductivitySigmaK
	 *            the foodProductivitySigmaK to set
	 */
	public void setFoodProductivitySigmaK(double foodProductivitySigmaK) {
		this.foodProductivitySigmaK = foodProductivitySigmaK;
	}

	/**
	 * @param foodResourcePreference
	 *            the foodResourcePreference to set
	 */
	public void setFoodResourcePreference(double foodResourcePreference) {
		this.foodResourcePreference = foodResourcePreference;
	}

	/**
	 * @param inactivePopulationPreference
	 *            the inactivePopulationPreference to set
	 */
	public void setInactivePopulationPreference(
			double inactivePopulationPreference) {
		this.inactivePopulationPreference = inactivePopulationPreference;
	}

	/**
	 * @param individualEducationProductivityRate
	 *            the individualEducationProductivityRate to set
	 */
	public void setIndividualEducationProductivityRate(
			double individualEducationProductivityRate) {
		this.individualEducationProductivityRate = individualEducationProductivityRate;
	}

	/**
	 * @param individualFoodProductivityRate
	 *            the individualFoodProductivityRate to set
	 */
	public void setIndividualFoodProductivityRate(
			double individualFoodProductivityRate) {
		this.individualFoodProductivityRate = individualFoodProductivityRate;
	}

	/**
	 * @param individualFoodRequest
	 *            the individualFoodRequest to set
	 */
	public void setIndividualFoodRequest(double individualFoodRequest) {
		this.individualFoodRequest = individualFoodRequest;
	}

	/**
	 * @param individualResearchProductivityRate
	 *            the individualResearchProductivityRate to set
	 */
	public void setIndividualResearchProductivityRate(
			double individualResearchProductivityRate) {
		this.individualResearchProductivityRate = individualResearchProductivityRate;
	}

	/**
	 * @param maximumResourceFoodProductivity
	 *            the maximumResourceFoodProductivity to set
	 */
	public void setMaximumResourceFoodProductivity(
			double maximumResourceFoodProductivity) {
		this.maximumResourceFoodProductivity = maximumResourceFoodProductivity;
	}

	/**
	 * @param minimumIndividualFoodProductivity
	 *            the minimumIndividualFoodProductivity to set
	 */
	public void setMinimumIndividualFoodProductivity(
			double minimumIndividualFoodProductivity) {
		this.minimumIndividualFoodProductivity = minimumIndividualFoodProductivity;
	}

	/**
	 * @param minimumIndividualResearchProductivity
	 *            the minimumIndividualResearchProductivity to set
	 */
	public void setMinimumIndividualResearchProductivity(
			double minimumIndividualResearchProductivity) {
		this.minimumIndividualResearchProductivity = minimumIndividualResearchProductivity;
	}

	/**
	 * @param minimumResourceEducationProductivity
	 *            the minimumResourceEducationProductivity to set
	 */
	public void setMinimumResourceEducationProductivity(
			double minimumResourceEducationProductivity) {
		this.minimumResourceEducationProductivity = minimumResourceEducationProductivity;
	}

	/**
	 * @param minimumResourceFoodProductivity
	 *            the minimumResourceFoodProductivity to set
	 */
	public void setMinimumResourceFoodProductivity(
			double minimumResourceFoodProductivity) {
		this.minimumResourceFoodProductivity = minimumResourceFoodProductivity;
	}

	/**
	 * @param minimumResourceResearchProductivity
	 *            the minimumResourceResearchProductivity to set
	 */
	public void setMinimumResourceResearchProductivity(
			double minimumResourceResearchProductivity) {
		this.minimumResourceResearchProductivity = minimumResourceResearchProductivity;
	}

	/**
	 * @param population
	 *            the population to set
	 */
	public void setPopulation(int population) {
		this.population = population;
	}

	/**
	 * @param preferedPopulationDensity
	 *            the preferedPopulationDensity to set
	 */
	public void setPreferedPopulationDensity(double preferedPopulationDensity) {
		this.preferedPopulationDensity = preferedPopulationDensity;
	}

	/**
	 * 
	 * @param random
	 */
	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * @param researchPopulationPreference
	 *            the researchPopulationPreference to set
	 */
	public void setResearchPopulationPreference(
			double researchPopulationPreference) {
		this.researchPopulationPreference = researchPopulationPreference;
	}

	/**
	 * @param researchReference
	 *            the researchReference to set
	 */
	public void setResearchReference(double researchReference) {
		this.researchReference = researchReference;
	}

	/**
	 * @param researchResourcePreference
	 *            the researchResourcePreference to set
	 */
	public void setResearchResourcePreference(double researchResourcePreference) {
		this.researchResourcePreference = researchResourcePreference;
	}

	/**
	 * @param resourceDeathRate
	 *            the resourceDeathRate to set
	 */
	public void setResourceDeathRate(double resourceDeathRate) {
		this.resourceDeathRate = resourceDeathRate;
	}

	/**
	 * @param resourceEducationProductivityRate
	 *            the resourceEducationProductivityRate to set
	 */
	public void setResourceEducationProductivityRate(
			double resourceEducationProductivityRate) {
		this.resourceEducationProductivityRate = resourceEducationProductivityRate;
	}

	/**
	 * @param resourceResearchProductivityRate
	 *            the resourceResearchProductivityRate to set
	 */
	public void setResourceResearchProductivityRate(
			double resourceResearchProductivityRate) {
		this.resourceResearchProductivityRate = resourceResearchProductivityRate;
	}

	/**
	 * @param settlementResourcePreference
	 *            the settlementResourcePreference to set
	 */
	public void setSettlementResourcePreference(
			double settlementResourcePreference) {
		this.settlementResourcePreference = settlementResourcePreference;
	}

	/**
	 * @param totalResource
	 *            the totalResource to set
	 */
	public void setTotalResource(double totalResource) {
		this.totalResource = totalResource;
	}
}