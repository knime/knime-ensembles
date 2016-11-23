package org.knime.ensembles.predictionfusion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.ensembles.predictionfusion.PredictionFusionNodeConfig.PredictionConfig;
import org.knime.ensembles.predictionfusion.methods.PredictionFusionMethodFactory;

/**
 * Prediction Fusion node dialog.
 *
 * @author Patrick Winter, University of Konstanz
 */
public class PredictionFusionNodeDialog extends NodeDialogPane {

	private DataTableSpec m_spec;
	private JComboBox<String> m_method = new JComboBox<String>();
	private DefaultListModel<String> m_classesModel = new DefaultListModel<String>();
	private JList<String> m_classes = new JList<String>(m_classesModel);
	private List<JSpinner> m_weights = new ArrayList<JSpinner>();
	private Map<String, List<ColumnSelectionComboxBox>> m_classConfidences = new HashMap<String, List<ColumnSelectionComboxBox>>();
	private JButton m_add = new JButton("Add prediction");
	private JPanel m_columnSelectionPanel = new JPanel(new GridBagLayout());
	private int m_nrPredictions = 0;

	/**
	 * Creates the dialog.
	 */
	public PredictionFusionNodeDialog() {
		m_columnSelectionPanel.setBackground(Color.WHITE);
		m_columnSelectionPanel.setBorder(new LineBorder(Color.BLACK));
		m_method.setModel(
				new DefaultComboBoxModel<String>(PredictionFusionMethodFactory.getAvailablePredictionFusionMethods()));
		m_add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				addPrediction();
			}
		});
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		panel.add(new JLabel("Method"), gbc);
		gbc.gridy++;
		panel.add(m_method, gbc);
		gbc.gridy++;
		panel.add(new JLabel("Classes"), gbc);
		gbc.gridy++;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(createClassesPanel(), gbc);
		gbc.gridy++;
		gbc.weighty = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		panel.add(new JLabel("Class confidences"), gbc);
		gbc.gridy++;
		gbc.weighty = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		panel.add(new JScrollPane(m_columnSelectionPanel), gbc);
		addTab("Config", panel, false);
	}

	/**
	 * Create the classes configuration panel.
	 *
	 * @return The panel
	 */
	private JPanel createClassesPanel() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		final JButton addFromColumn = new JButton("+");
        addFromColumn
            .addActionListener(e -> openClassesFromDomainDialog(addFromColumn).stream().forEach(c -> addClass(c)));
		final JButton add = new JButton("+");
		add.addActionListener(e -> addClass(openInputDialog("Add class", "", add)));
		final JButton edit = new JButton("✎");
		edit.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				int position = m_classes.getSelectedIndex();
				// if nothing is selected we ignore it
				if (position >= 0) {
					String oldClass = m_classes.getSelectedValue();
					String newClass = openInputDialog("Edit class", oldClass, add);
					// if input is empty we do nothing
					if (!newClass.isEmpty()) {
						boolean found = false;
						for (int i = 0; i < m_classesModel.getSize(); i++) {
							if (m_classesModel.get(i).equals(newClass) && i != position) {
								found = true;
								break;
							}
						}
						// if new input already exists we do nothing
						if (!found) {
							m_classesModel.set(position, newClass);
							m_classes.revalidate();
							m_classConfidences.put(newClass, m_classConfidences.get(oldClass));
							m_classConfidences.remove(oldClass);
							updateConfidencePanel();
						}
					}
				}
			}
		});
		JButton remove = new JButton("✖");
		remove.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				int position = m_classes.getSelectedIndex();
				// if nothing is selected we do nothing
				if (position >= 0) {
					String cls = m_classesModel.remove(position);
					// remove column selectors for this class
					m_classConfidences.remove(cls);
					m_classes.revalidate();
					if (position < m_classesModel.size()) {
						m_classes.setSelectedIndex(position);
					}
					updateConfidencePanel();
				}
			}
		});
		JButton up = new JButton("▲");
		up.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				int position = m_classes.getSelectedIndex();
				// only move up if we have a selection (position >= 0) and the
				// new position would be valid (position - 1 >= 0)
				if (position > 0) {
					// remove class from current position
					String cls = m_classesModel.remove(position);
					// add class again one position higher
					m_classesModel.add(position - 1, cls);
					m_classes.revalidate();
					m_classes.setSelectedIndex(position - 1);
					// since order of classes has changed we need to redo the
					// confidence panel layout
					updateConfidencePanel();
				}
			}
		});
		JButton down = new JButton("▼");
		down.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(final ActionEvent e) {
				int position = m_classes.getSelectedIndex();
				// only move down if we have a selection (position >= 0) and the
				// new position would be valid (position + 1 < nrClasses)
				if (position >= 0 && position + 1 < m_classesModel.size()) {
					// remove class from current position
					String cls = m_classesModel.remove(position);
					// add class again one position lower
					m_classesModel.add(position + 1, cls);
					m_classes.revalidate();
					m_classes.setSelectedIndex(position + 1);
					// since order of classes has changed we need to redo the
					// confidence panel layout
					updateConfidencePanel();
				}
			}
		});
		// buttons should be small
		Insets buttonMargin = new Insets(0, 3, 0, 3);
		addFromColumn.setMargin(buttonMargin);
		up.setMargin(buttonMargin);
		down.setMargin(buttonMargin);
		add.setMargin(buttonMargin);
		edit.setMargin(buttonMargin);
		remove.setMargin(buttonMargin);
		// descriptive tooltips
		addFromColumn.setToolTipText("Add classes from column domain");
		up.setToolTipText("Move class up");
		down.setToolTipText("Move class down");
		add.setToolTipText("Add new class");
		edit.setToolTipText("Edit selected class");
		remove.setToolTipText("Remove selected class");
		buttonPanel.add(addFromColumn, gbc);
		gbc.gridy++;
		gbc.insets = new Insets(5, 5, 5, 5);
        buttonPanel.add(up, gbc);
        gbc.gridy++;
		buttonPanel.add(down, gbc);
		gbc.gridy++;
		buttonPanel.add(add, gbc);
		gbc.gridy++;
		buttonPanel.add(edit, gbc);
		gbc.gridy++;
		buttonPanel.add(remove, gbc);
		gbc.gridy++;
		gbc.weighty = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		buttonPanel.add(new JPanel(), gbc);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.WHITE);
		panel.add(new JScrollPane(m_classes), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.EAST);
		return panel;
	}

	private void addClass(final String newClass) {
        // ignore if input is empty
        if (!newClass.isEmpty()) {
            // check if class already exists
            boolean found = false;
            for (int i = 0; i < m_classesModel.getSize(); i++) {
                if (m_classesModel.get(i).equals(newClass)) {
                    found = true;
                    break;
                }
            }
            // only add if it doesn't already exist
            if (!found) {
                m_classesModel.addElement(newClass);
                m_classes.revalidate();
                m_classes.setSelectedIndex(m_classesModel.size() - 1);
                // add column selectors for this class to predictions
                m_classConfidences.put(newClass, new ArrayList<ColumnSelectionComboxBox>());
                for (int i = 0; i < m_nrPredictions; i++) {
                    m_classConfidences.get(newClass).add(createColumnSelection());
                }
                updateConfidencePanel();
            }
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		PredictionFusionNodeConfig config = new PredictionFusionNodeConfig();
		config.setMethod((String) m_method.getSelectedItem());
		String[] classes = new String[m_classes.getModel().getSize()];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = m_classes.getModel().getElementAt(i);
		}
		config.setClasses(classes);
		// translate GUI elements for predictions into PredictionConfig array
		PredictionConfig[] predictions = new PredictionConfig[m_nrPredictions];
		for (int i = 0; i < predictions.length; i++) {
			String[] columns = new String[classes.length];
			for (int j = 0; j < columns.length; j++) {
				columns[j] = m_classConfidences.get(classes[j]).get(i).getSelectedColumn();
			}
			predictions[i] = new PredictionConfig((int) m_weights.get(i).getValue(), columns);
		}
		config.setPredictions(predictions);
		config.save(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {
		m_spec = specs[0];
		PredictionFusionNodeConfig config = new PredictionFusionNodeConfig();
		config.loadWithDefaults(settings);
		m_method.setSelectedItem(config.getMethod());
		m_classesModel.clear();
		m_classConfidences.clear();
		String[] classes = config.getClasses();
		for (String cls : classes) {
			m_classesModel.addElement(cls);
			m_classConfidences.put(cls, new ArrayList<ColumnSelectionComboxBox>());
		}
		m_classes.revalidate();
		// create GUI elements for PredictionConfig array
		PredictionConfig[] predictions = config.getPredictions();
		for (int i = 0; i < predictions.length; i++) {
			PredictionConfig prediction = predictions[i];
			JSpinner weight = createWeightSpinner();
			weight.setValue(prediction.getWeight());
			m_weights.add(weight);
			String[] columns = prediction.getColumns();
			for (int j = 0; j < classes.length; j++) {
				String cls = classes[j];
				ColumnSelectionComboxBox confidenceColumn = createColumnSelection();
				confidenceColumn.setSelectedColumn(columns[j]);
				m_classConfidences.get(cls).add(confidenceColumn);
			}
		}
		m_nrPredictions = predictions.length;
		updateConfidencePanel();
	}

	/**
	 * Adds a new prediction row to the confidence panel.
	 */
	private void addPrediction() {
		// add one weight for this prediction
		m_weights.add(createWeightSpinner());
		// add column selector for each class
		for (int i = 0; i < m_classesModel.getSize(); i++) {
			m_classConfidences.get(m_classesModel.getElementAt(i)).add(createColumnSelection());
		}
		m_nrPredictions++;
		// redo layout of confidence panel
		updateConfidencePanel();
	}

	/**
	 * Removes a prediction from the confidence panel.
	 *
	 * @param index
	 *            Row index of the prediction
	 */
	private void removePrediction(final int index) {
		// remove weight
		m_weights.remove(index);
		// remove column selection for each class
		for (int i = 0; i < m_classesModel.getSize(); i++) {
			m_classConfidences.get(m_classesModel.getElementAt(i)).remove(index);
		}
		m_nrPredictions--;
		// redo layout of confidence panel
		updateConfidencePanel();
	}

	/**
	 * Creates a JSpinner component for a predictions weight.
	 *
	 * @return The weight spinner
	 */
	private JSpinner createWeightSpinner() {
		JSpinner weight = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		// spinner is way to big by default
		weight.setPreferredSize(new Dimension(50, weight.getPreferredSize().height));
		return weight;
	}

	/**
	 * Creates a column selection component for a predictions confidence value.
	 *
	 * @return The confidence column selection component
	 */
	@SuppressWarnings("unchecked")
	private ColumnSelectionComboxBox createColumnSelection() {
		ColumnSelectionComboxBox classConfidence = new ColumnSelectionComboxBox((Border) null, DoubleValue.class);
		try {
			classConfidence.update(m_spec, null);
		} catch (NotConfigurableException e) {
		}
		// column selection can get very big if input table contains a big
		// column
		classConfidence.setPreferredSize(new Dimension(200, classConfidence.getPreferredSize().height));
		return classConfidence;
	}

	/**
	 * Rearranges the confidence panel after changes have been made. Possible
	 * changes are adding or removing predictions and adding, removing or
	 * rearranging classes.
	 */
	private void updateConfidencePanel() {
		m_columnSelectionPanel.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		// add weight column
		m_columnSelectionPanel.add(new JLabel("Weight"), gbc);
		gbc.gridy++;
		for (int i = 0; i < m_nrPredictions; i++) {
			m_columnSelectionPanel.add(m_weights.get(i), gbc);
			gbc.gridy++;
		}
		gbc.gridx = 1;
		// add column selection panels for all classes
		for (int i = 0; i < m_classesModel.getSize(); i++) {
			String cls = m_classesModel.getElementAt(i);
			gbc.gridy = 0;
			m_columnSelectionPanel.add(new JLabel(cls), gbc);
			gbc.gridy++;
			for (int j = 0; j < m_nrPredictions; j++) {
				m_columnSelectionPanel.add(m_classConfidences.get(cls).get(j), gbc);
				gbc.gridy++;
			}
			gbc.gridx++;
		}
		gbc.gridx = 0;
		gbc.gridwidth = m_classesModel.getSize() + 2;
		gbc.fill = GridBagConstraints.NONE;
		m_columnSelectionPanel.add(m_add, gbc);
		gbc.gridx = m_classesModel.getSize() + 1;
		gbc.gridy = 1;
		// add remove buttons
		for (int i = 0; i < m_nrPredictions; i++) {
			final int rowIndex = i;
			final JButton remove = new JButton("✖");
			remove.setSize(remove.getSize().height, remove.getSize().height);
			remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removePrediction(rowIndex);
				}
			});
			remove.setMargin(new Insets(0, 3, 0, 3));
			m_columnSelectionPanel.add(remove, gbc);
			gbc.gridy++;
		}
		m_columnSelectionPanel.revalidate();
		m_columnSelectionPanel.repaint();
		m_columnSelectionPanel.getParent().revalidate();
		m_columnSelectionPanel.getParent().repaint();
	}

	/**
	 * Opens a dialog with a string input field.
	 *
	 * @param title
	 *            Title of the dialog window
	 * @param defaultText
	 *            Default text in the input field
	 * @param relativeTo
	 *            Anchor for the dialogs placement
	 * @return The input string or null if the dialog was canceled
	 */
	private String openInputDialog(final String title, final String defaultText, final Component relativeTo) {
		Frame f = null;
		Container c = getPanel().getParent();
		while (c != null) {
			if (c instanceof Frame) {
				f = (Frame) c;
				break;
			}
			c = c.getParent();
		}
		final JDialog dialog = new JDialog(f);
		final AtomicBoolean apply = new AtomicBoolean(false);
		JTextField name = new JTextField();
		name.setText(defaultText);
		name.setColumns(30);
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		Insets buttonMargin = new Insets(0, 0, 0, 0);
		ok.setMargin(buttonMargin);
		cancel.setMargin(buttonMargin);
		// cancel and close dialog on escape
		dialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
				}
			}
		});
		name.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(final KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					// enter in text field closes the dialog
					apply.set(true);
					dialog.setVisible(false);
				} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					dialog.setVisible(false);
				}
			}
		});
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				apply.set(true);
				dialog.setVisible(false);
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		ok.setPreferredSize(cancel.getPreferredSize());
		dialog.setLayout(new GridBagLayout());
		dialog.setTitle(title);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		dialog.add(name, gbc);
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		gbc.gridwidth = 1;
		gbc.gridy++;
		dialog.add(new JLabel(), gbc);
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx++;
		dialog.add(ok, gbc);
		gbc.gridx++;
		dialog.add(cancel, gbc);
		dialog.pack();
		dialog.setLocationRelativeTo(relativeTo);
		dialog.setModal(true);
		dialog.setVisible(true);
		// continues here after dialog is closed
		String input = null;
		// if canceled the return value stays null
		if (apply.get()) {
			// we have to get the text before we dispose of the dialog
			input = name.getText();
		}
		dialog.dispose();
		return input;
	}

	/**
	 * Opens a dialog with a column selection panel.
	 *
	 * @param relativeTo Anchor for the dialogs placement
	 * @return List of possible values contained in the selected column's domain. If the dialog is canceled an empty list will be returned.
	 */
    private List<String> openClassesFromDomainDialog(final Component relativeTo) {
        Frame f = null;
        Container c = getPanel().getParent();
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame) c;
                break;
            }
            c = c.getParent();
        }
        final JDialog dialog = new JDialog(f);
        final AtomicBoolean apply = new AtomicBoolean(false);
        @SuppressWarnings("unchecked")
        ColumnSelectionComboxBox column = new ColumnSelectionComboxBox((Border)null, StringValue.class);
        try {
            column.update(m_spec, null);
        } catch (NotConfigurableException e1) {
        }
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        Insets buttonMargin = new Insets(0, 0, 0, 0);
        ok.setMargin(buttonMargin);
        cancel.setMargin(buttonMargin);
        // cancel and close dialog on escape
        dialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
                    dialog.setVisible(false);
                }
            }
        });
        ok.addActionListener(e -> {
            apply.set(true);
            dialog.setVisible(false);
        });
        cancel.addActionListener(e -> dialog.setVisible(false));
        ok.setPreferredSize(cancel.getPreferredSize());
        dialog.setLayout(new GridBagLayout());
        dialog.setTitle("Add classes from class column domain");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        dialog.add(column, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.gridwidth = 1;
        gbc.gridy++;
        dialog.add(new JLabel(), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx++;
        dialog.add(ok, gbc);
        gbc.gridx++;
        dialog.add(cancel, gbc);
        dialog.pack();
        dialog.setLocationRelativeTo(relativeTo);
        dialog.setModal(true);
        dialog.setVisible(true);

        List<String> values;
        if (apply.get()) {
            values = m_spec.getColumnSpec(column.getSelectedColumn()).getDomain().getValues().stream()
                .filter(cell -> !cell.isMissing())
                .map(cell -> ((StringValue)cell).getStringValue())
                .collect(Collectors.toList());
        } else {
            values = Collections.emptyList();
        }
        dialog.dispose();
        return values;
    }

}
