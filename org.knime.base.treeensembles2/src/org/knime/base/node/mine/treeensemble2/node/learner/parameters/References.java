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

import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.updates.ParameterReference;

@SuppressWarnings({"MissingJavadoc", "java:S1176"})
public final class References {

    private References() {
        // no instance
    }

    protected interface ColumnSamplingModeWidgetRef extends Modification.Reference {
    }

    protected interface IgnoreColumnsWithoutDomainWidgetRef extends Modification.Reference {
    }

    protected interface AttributeReuseWidgetRef extends Modification.Reference {
    }

    protected interface BinaryNominalSplitsWidgetRef extends Modification.Reference {
    }

    protected interface MaxTreeDepthWidgetRef extends Modification.Reference {
    }

    protected interface FixedRootAttributeWidgetRef extends Modification.Reference {
    }

    protected interface ColumnAbsoluteWidgetRef extends Modification.Reference {
    }

    protected interface ColumnFractionWidgetRef extends Modification.Reference {
    }

    interface ColumnSamplingModeRef extends ParameterReference<Options.ColumnSamplingModeOption> {
    }

    protected interface HiliteCountRef extends Modification.Reference {
    }

    protected interface MissingValueHandlingWidgetRef extends Modification.Reference {
    }

    protected interface SaveTargetDistributionRef extends Modification.Reference {
    }

    protected interface NrModelsRef extends Modification.Reference {
    }

    protected interface RowSamplingFractionWidgetRef extends Modification.Reference {
    }

    protected interface RowSamplingReplacementWidgetRef extends Modification.Reference {
    }

    protected interface RowSamplingModeWidgetRef extends Modification.Reference {
    }

    protected interface SeedRef extends Modification.Reference {
    }

    protected interface SplitCriterionWidgetRef extends Modification.Reference {
    }

    interface TargetColumnValueRef extends ParameterReference<String> {
    }

    protected interface TargetColumnWidgetRef extends Modification.Reference {
    }
}
