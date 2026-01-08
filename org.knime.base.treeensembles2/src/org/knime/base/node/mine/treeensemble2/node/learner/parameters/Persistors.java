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

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSamplerFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.legacy.EnumBooleanPersistor;
import org.knime.node.parameters.persistence.legacy.LegacyColumnFilterPersistor;

/**
 * Persistors for the tree ensemble learner options.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public final class Persistors {

    private Persistors() {
        // prevent instantiation
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

    static final class RowSamplingModePersistor implements NodeParametersPersistor<EnumOptions.RowSamplingModeOption> {
        @Override
        public EnumOptions.RowSamplingModeOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = RowSamplerFactory.RowSamplingMode
                .valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE,
                    RowSamplerFactory.RowSamplingMode.Random.name()));
            return EnumOptions.RowSamplingModeOption.fromLegacy(legacy);
        }

        @Override
        public void save(final EnumOptions.RowSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE}};
        }
    }

    /**
     * Persistor for the column sampling mode.
     *
     * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
     */
    public static final class ColumnSamplingModePersistor
        implements NodeParametersPersistor<EnumOptions.ColumnSamplingModeOption> {
        @Override
        public EnumOptions.ColumnSamplingModeOption load(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            final var stringValue = settings.getString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE,
                TreeEnsembleLearnerConfiguration.DEF_COLUMN_SAMPLING_MODE.name());
            try {
                var legacy = TreeEnsembleLearnerConfiguration.ColumnSamplingMode.valueOf(stringValue);
                return EnumOptions.ColumnSamplingModeOption.fromLegacy(legacy);
            } catch (IllegalArgumentException iae) {
                throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(
                    TreeEnsembleLearnerConfiguration.ColumnSamplingMode.class, stringValue), iae);
            }
        }

        @Override
        public void save(final EnumOptions.ColumnSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE}};
        }
    }

    /**
     * Persistor for the attribute reuse option.
     *
     * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
     */
    @SuppressWarnings("restriction")
    public static final class AttributeReusePersistor extends EnumBooleanPersistor<EnumOptions.AttributeReuseOption> {

        AttributeReusePersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE,
                EnumOptions.AttributeReuseOption.class, EnumOptions.AttributeReuseOption.DIFFERENT_FOR_EACH_NODE);
        }

    }

    static final class SeedPersistor implements NodeParametersPersistor<Optional<String>> {
        @Override
        public Optional<String> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var string = settings.getString(TreeEnsembleLearnerConfiguration.KEY_SEED, null);
            if (string == null) {
                return Optional.empty();
            }
            try {
                Long.parseLong(string);
            } catch (NumberFormatException e) {
                throw new InvalidSettingsException(e);
            }
            return Optional.of(string);
        }

        @Override
        public void save(final Optional<String> value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SEED, value.orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SEED}};
        }
    }

    @SuppressWarnings("restriction")
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
        implements NodeParametersPersistor<EnumOptions.MissingValueHandlingOption> {
        @Override
        public EnumOptions.MissingValueHandlingOption load(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            final var stringValue = settings.getString(TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING,
                TreeEnsembleLearnerConfiguration.MissingValueHandling.XGBoost.name());
            try {

                var legacy = TreeEnsembleLearnerConfiguration.MissingValueHandling.valueOf(stringValue);
                return EnumOptions.MissingValueHandlingOption.fromLegacy(legacy);
            } catch (IllegalArgumentException iae) {
                throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(
                    TreeEnsembleLearnerConfiguration.MissingValueHandling.class, stringValue), iae);
            }
        }

        @Override
        public void save(final EnumOptions.MissingValueHandlingOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING}};
        }
    }

    /**
     * Persistor for the max depth parameter.
     *
     * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
     */
    public static final class MaxDepthPersistor extends OptionalEmptyAsSpecialIntPersistor {

        MaxDepthPersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS, TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE);
        }

    }

    static final class SplitCriterionPersistor implements NodeParametersPersistor<EnumOptions.SplitCriterionOption> {
        @Override
        public EnumOptions.SplitCriterionOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var stringValue = settings.getString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION,
                TreeEnsembleLearnerConfiguration.SplitCriterion.Gini.name());
            try {
                var legacy = TreeEnsembleLearnerConfiguration.SplitCriterion.valueOf(stringValue);
                return EnumOptions.SplitCriterionOption.fromLegacy(legacy);
            } catch (IllegalArgumentException iae) {
                throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(
                    TreeEnsembleLearnerConfiguration.SplitCriterion.class, stringValue), iae);
            }
        }

        @Override
        public void save(final EnumOptions.SplitCriterionOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION}};
        }
    }

    private static <E extends Enum<E>> String createInvalidSettingsExceptionMessage(final Class<E> enumClass,
        final String name) {
        var values = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
        return String.format("Invalid value '%s'. Possible values: %s", name, values);
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

    abstract static class OptionalEmptyAsSpecialIntPersistor implements NodeParametersPersistor<Optional<Integer>> {

        private final String m_configKey;

        private final int m_specialInt;

        OptionalEmptyAsSpecialIntPersistor(final String configKey, final int specialInt) {
            m_configKey = configKey;
            m_specialInt = specialInt;
        }

        @Override
        public Optional<Integer> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var value = settings.getInt(m_configKey, m_specialInt);
            if (value == m_specialInt) {
                return Optional.empty();
            }
            return Optional.of(value);
        }

        @Override
        public void save(final Optional<Integer> value, final NodeSettingsWO settings) {
            settings.addInt(m_configKey, value.orElse(m_specialInt));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{m_configKey}};
        }
    }
}
