/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   Sep 25, 2014 (Patrick Winter): created
 */
package org.knime.ensembles.predictionfusion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ensembles.predictionfusion.PredictionFusionNodeConfig.PredictionConfig;
import org.knime.ensembles.predictionfusion.methods.Prediction;
import org.knime.ensembles.predictionfusion.methods.PredictionFusionMethod;
import org.knime.ensembles.predictionfusion.methods.PredictionFusionMethodFactory;

/**
 * Prediction Fusion node model.
 *
 * @author Patrick Winter, University of Konstanz
 */
public class PredictionFusionNodeModel extends SimpleStreamableFunctionNodeModel {

	private PredictionFusionNodeConfig m_config = new PredictionFusionNodeConfig();

	/**
     * {@inheritDoc}
	 */
	@Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec inSpecs) {
        final PredictionFusionMethod method = PredictionFusionMethodFactory.getPredictionFusionMethod(m_config.getMethod());
		ColumnRearranger rearranger = new ColumnRearranger(inSpecs);
		final String[] classes = m_config.getClasses();
		final PredictionConfig[] predictionConfigs = m_config.getPredictions();
		// find index for columns, first index is prediction second index is
		// class
		final int[][] columnIndex = new int[predictionConfigs.length][];
		for (int i = 0; i < columnIndex.length; i++) {
			columnIndex[i] = new int[classes.length];
			for (int j = 0; j < columnIndex[i].length; j++) {
				String column = predictionConfigs[i].getColumns()[j];
				columnIndex[i][j] = inSpecs.findColumnIndex(column);
			}
		}
		CellFactory cellFactory = new AbstractCellFactory() {
			@Override
            public DataColumnSpec[] getColumnSpecs() {
				// added columns are one for each classes confidence and a
				// winning class
				DataColumnSpec[] columnSpecs = new DataColumnSpec[classes.length + 1];
				for (int i = 0; i < classes.length; i++) {
					columnSpecs[i] = new DataColumnSpecCreator(
							DataTableSpec.getUniqueColumnName(inSpecs, "Fused confidence - " + classes[i]),
							DoubleCell.TYPE).createSpec();
				}
				columnSpecs[columnSpecs.length - 1] = new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(inSpecs, "Fused prediction"), StringCell.TYPE).createSpec();
				return columnSpecs;
			}

			@Override
            public DataCell[] getCells(final DataRow row) {
				// create prediction objects containing all class confidences
				Prediction[] predictions = new Prediction[predictionConfigs.length];
				int[] weights = new int[predictionConfigs.length];
				for (int i = 0; i < predictions.length; i++) {
					Prediction prediction = new Prediction();
					for (int j = 0; j < classes.length; j++) {
						DataCell cell = row.getCell(columnIndex[i][j]);
						// missing values are ignored
						if (!cell.isMissing() && cell.getType().isCompatible(DoubleValue.class)) {
							double confidence = ((DoubleValue) cell).getDoubleValue();
							if (confidence < 0) {
								throw new RuntimeException("No negative confidence values allowed. Encountered value "
										+ confidence + " (row: '" + row.getKey().getString() + "', column: '"
										+ inSpecs.getColumnSpec(columnIndex[i][j]).getName() + "').");
							}
							prediction.setClassConfidence(classes[j], confidence);
						} else if (cell.isMissing()) {
						    PredictionFusionNodeModel.this.setWarningMessage("Found missing values in input confidences. Missing values were skipped.");
						}
					}
					weights[i] = predictionConfigs[i].getWeight();
					predictions[i] = prediction;
				}
				// fuse predictions into one
				Prediction prediction = method.fusePredictions(predictions, weights);
				DataCell[] cells = new DataCell[classes.length + 1];
				for (int i = 0; i < classes.length; i++) {
					Double confidence = prediction.getClassConfidence(classes[i], true);
					// if there was no valid confidence in the input we will not
					// have a valid fusion confidence
					cells[i] = confidence == null ? new MissingCell(null) : new DoubleCell(confidence);
				}
				String predictedClass = prediction.getPredictedClass(classes);
				// if we have no valid confidences we don't have a predicted
				// class
				cells[cells.length - 1] = predictedClass == null ? new MissingCell(null)
						: new StringCell(predictedClass);
				return cells;
			}
		};
		rearranger.append(cellFactory);
		return rearranger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (!Arrays.asList(PredictionFusionMethodFactory.getAvailablePredictionFusionMethods())
				.contains(m_config.getMethod())) {
			// the configured method is unknown (should not happen)
			throw new InvalidSettingsException("No valid method selected.");
		}
		PredictionConfig[] predictions = m_config.getPredictions();
		List<String> columns = new ArrayList<String>();
		for (PredictionConfig prediction : predictions) {
			columns.addAll(Arrays.asList(prediction.getColumns()));
		}
		for (String column : columns) {
			int columnIndex = inSpecs[0].findColumnIndex(column);
			// check if all configured columns are available
			if (columnIndex < 0) {
				throw new InvalidSettingsException("The previously selected confidence column '" + column + "' does not exist in the input table.");
			}
			// check if all configured columns are compatible with double
			if (!inSpecs[0].getColumnSpec(columnIndex).getType().isCompatible(DoubleValue.class)) {
				throw new InvalidSettingsException(
						"The previously selected confidence column '" + column + "' is not compatible with the data type double.");
			}
		}
		ColumnRearranger rearranger = createColumnRearranger(inSpecs[0]);
		return new DataTableSpec[] { rearranger.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_config.save(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		PredictionFusionNodeConfig config = new PredictionFusionNodeConfig();
		config.load(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		PredictionFusionNodeConfig config = new PredictionFusionNodeConfig();
		config.load(settings);
		m_config = config;
	}

}
