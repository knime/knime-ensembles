/*
 * ------------------------------------------------------------------------
 *
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
 */
package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.Optional;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSamplerFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.legacy.LegacyColumnFilterPersistor;

@SuppressWarnings({"MissingJavadoc", "java:S1176"})
public final class Persistors {

    private Persistors() {

    }

    static final class HardCodedRootPersistor implements NodeParametersPersistor<Optional<String>> {
        @Override
        public Optional<String> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Optional.ofNullable(settings.getString(TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN, null));
        }

        @Override
        public void save(final Optional<String> value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN, value.orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN}};
        }
    }

    static final class RowSamplingFractionPersistor implements NodeParametersPersistor<Optional<Double>> {
        @Override
        public Optional<Double> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var fraction = settings.getDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION,
                TreeEnsembleLearnerConfiguration.DEF_DATA_FRACTION);
            return fraction < 1.0 ? Optional.of(fraction) : Optional.empty();
        }

        @Override
        public void save(final Optional<Double> value, final NodeSettingsWO settings) {
            settings.addDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION, value.orElse(1.0));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION}};
        }
    }

    static final class RowSamplingReplacementPersistor implements NodeParametersPersistor<Boolean> {
        @Override
        public Boolean load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT, true);
        }

        @Override
        public void save(final Boolean value, final NodeSettingsWO settings) {
            settings.addBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT,
                Boolean.TRUE.equals(value));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT}};
        }
    }

    static final class RowSamplingModePersistor implements NodeParametersPersistor<Options.RowSamplingModeOption> {
        @Override
        public Options.RowSamplingModeOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = RowSamplerFactory.RowSamplingMode
                .valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE,
                    RowSamplerFactory.RowSamplingMode.Random.name()));
            return Options.RowSamplingModeOption.fromLegacy(legacy);
        }

        @Override
        public void save(final Options.RowSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE}};
        }
    }

    static final class ColumnSamplingModePersistor
        implements NodeParametersPersistor<Options.ColumnSamplingModeOption> {
        @Override
        public Options.ColumnSamplingModeOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = TreeEnsembleLearnerConfiguration.ColumnSamplingMode
                .valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE,
                    TreeEnsembleLearnerConfiguration.DEF_COLUMN_SAMPLING_MODE.name()));
            return Options.ColumnSamplingModeOption.fromLegacy(legacy);
        }

        @Override
        public void save(final Options.ColumnSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE}};
        }
    }

    static final class ColumnAbsolutePersistor implements NodeParametersPersistor<Integer> {
        @Override
        public Integer load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getInt(TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE,
                TreeEnsembleLearnerConfiguration.DEF_COLUMN_ABSOLUTE);
        }

        @Override
        public void save(final Integer value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE}};
        }
    }

    static final class AttributeReusePersistor implements NodeParametersPersistor<Options.AttributeReuseOption> {
        @Override
        public Options.AttributeReuseOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Options.AttributeReuseOption.fromBoolean(settings
                .getBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE, true));
        }

        @Override
        public void save(final Options.AttributeReuseOption value, final NodeSettingsWO settings) {
            settings.addBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE,
                value.toBoolean());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE}};
        }
    }

    static final class SeedPersistor implements NodeParametersPersistor<Optional<Long>> {
        @Override
        public Optional<Long> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var seedString = settings.getString(TreeEnsembleLearnerConfiguration.KEY_SEED, null);
            if (seedString == null || seedString.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Long.parseLong(seedString));
            } catch (NumberFormatException nfe) {
                throw new InvalidSettingsException("Unable to parse seed \"" + seedString + "\"", nfe);
            }
        }

        @Override
        public void save(final Optional<Long> value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SEED, value.map(Object::toString).orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SEED}};
        }
    }

    static final class ColumnFilterPersistor extends LegacyColumnFilterPersistor {
        ColumnFilterPersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FILTER_CONFIG);
        }
    }

    static final class ColumnFractionPersistor implements NodeParametersPersistor<Double> {
        @Override
        public Double load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getDouble(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR,
                TreeEnsembleLearnerConfiguration.DEF_COLUMN_FRACTION);
        }

        @Override
        public void save(final Double value, final NodeSettingsWO settings) {
            settings.addDouble(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR}};
        }
    }

    static final class HiliteCountPersistor implements NodeParametersPersistor<Optional<Integer>> {
        @Override
        public Optional<Integer> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var value = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS, -1);
            return value > 0 ? Optional.of(value) : Optional.empty();
        }

        @Override
        public void save(final Optional<Integer> value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS, value.orElse(-1));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS}};
        }
    }

    static final class MissingValueHandlingPersistor
        implements NodeParametersPersistor<Options.MissingValueHandlingOption> {
        @Override
        public Options.MissingValueHandlingOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = TreeEnsembleLearnerConfiguration.MissingValueHandling
                .valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING,
                    TreeEnsembleLearnerConfiguration.MissingValueHandling.XGBoost.name()));
            return Options.MissingValueHandlingOption.fromLegacy(legacy);
        }

        @Override
        public void save(final Options.MissingValueHandlingOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING}};
        }
    }

    static final class MaxDepthPersistor implements NodeParametersPersistor<Optional<Integer>> {
        @Override
        public Optional<Integer> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var value = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS,
                TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE);
            return value == TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE ? Optional.empty() : Optional.of(value);
        }

        @Override
        public void save(final Optional<Integer> value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS,
                value.orElse(TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS}};
        }
    }

    static final class SplitCriterionPersistor implements NodeParametersPersistor<Options.SplitCriterionOption> {
        @Override
        public Options.SplitCriterionOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = TreeEnsembleLearnerConfiguration.SplitCriterion
                .valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION,
                    TreeEnsembleLearnerConfiguration.SplitCriterion.Gini.name()));
            return Options.SplitCriterionOption.fromLegacy(legacy);
        }

        @Override
        public void save(final Options.SplitCriterionOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION}};
        }
    }

    static final class TargetColumnPersistor implements NodeParametersPersistor<String> {
        @Override
        public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getString(TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN, null);
        }

        @Override
        public void save(final String value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN}};
        }
    }
}
