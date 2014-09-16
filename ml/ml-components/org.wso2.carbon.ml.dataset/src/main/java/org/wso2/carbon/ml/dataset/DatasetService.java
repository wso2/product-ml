package org.wso2.carbon.ml.dataset;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ml.db.H2Connector;

public class DatasetService {
	private static final Log LOGGER = LogFactory.getLog(DatasetService.class);

	public int importData(String source) throws Exception {
		H2Connector h2Connector;
		Connection connection = null;
		try {
			h2Connector = H2Connector.initialize();
			connection = h2Connector.getConnection();
			// get the default uploading location
			ResultSet result =
					connection.createStatement()
					.executeQuery("SELECT DATASET_UPLOADING_DIR FROM ML_CONFIGURATION");

			// if found
			if (result.first()) {
				// update the dataset table
				String uri = result.getNString("DATASET_UPLOADING_DIR") + "/" + source;
				Statement insert = connection.createStatement();
				// insert the details to the table
				insert.execute("INSERT INTO ML_Dataset(URI) VALUES('" + uri + "');");
				// get the latest auto-generated Id
				ResultSet latestID = insert.getGeneratedKeys();
				latestID.first();
				return Integer.parseInt(latestID.getNString(1));
			} else {
				LOGGER.error("Default uploading location is not set in the ML_CONFIGURATION database table.");
			}
		} catch (Exception e) {
			String msg =
					"Error occured while updating the data-source details in the database. " +
							e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		}finally{
			if(connection!=null){
				connection.close();
			}
		}
		return -1;
	}

	/*
	 * Calculate summary stats and populate the database
	 */
	public void generateSummaryStats(String dataSource, int dataSourceID, int noOfRecords,
	                                 int noOfIntervals, String seperator)
	                                		 throws DatasetServiceException {
		DatasetSummary summary = new DatasetSummary();
		try {
			summary.calculateSummary(dataSourceID, noOfRecords, noOfIntervals, seperator);
			summary.updateDatabase(dataSourceID);
		} catch (DatasetServiceException e) {
			String msg = "Summary Statistics calculation failed. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (Exception e) {
			String msg = "Database update failed. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		}
	}

	/*
	 * Update feature with the given details
	 */
	public boolean updateFeature(String name, String type, ImputeOption imputeOption,
	                             boolean important) throws Exception {
		H2Connector h2Connector;
		Connection connection = null;
		try {
			h2Connector = H2Connector.initialize();
			connection = h2Connector.getConnection();
			return connection.createStatement().execute("UPDATE  ML_FEATURE SET TYPE ='" + type +
			                                            "',IMPUTE_METHOD='" + imputeOption.getMethod() +
			                                            "', IMPORTANT=" + important +
			                                            " WHERE name='" + name + "';");
		} catch (SQLException e) {
			String msg = "Error occured while updating the feature info. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		} catch (Exception e) {
			String msg = "Error occured while connecting to database. " + e.getMessage();
			LOGGER.error(msg, e);
			throw new DatasetServiceException(msg);
		} finally{
			if(connection!=null){
				connection.close();
			}
		}
	}

	/*
	 * Get summary statistics of a data set from the database
	 */
	public String getSummaryStats(String dataSource, int noOfRecords) throws IOException {
		// TODO
		String summary = "";
		return summary;
	}

	public List<Object> getSamplePoints(String feature1, String feature2, int maxNoOfPoints,
	                                    String selectionPolicy) {
		// TODO
		return null;
	}

	public List<Object> getSampleDistribution(String feature, int noOfBins) {
		// TODO
		return null;
	}
}
