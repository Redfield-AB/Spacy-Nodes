/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

import se.redfield.textprocessing.core.model.SpacyModelDescription;

/**
 * The spec for the SpaCy model port object.
 * 
 * @author Alexander Bondaletov
 *
 */
public class SpacyModelPortObjectSpec extends AbstractSimplePortObjectSpec {// NOSONAR
	/**
	 * The serializer for the {@link SpacyModelPortObjectSpec}
	 *
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<SpacyModelPortObjectSpec> {
	}

	private final SpacyModelDescription model;

	/**
	 * Default constructor.
	 */
	public SpacyModelPortObjectSpec() {
		this(new SpacyModelDescription());
	}

	/**
	 * @param model The model description.
	 */
	public SpacyModelPortObjectSpec(SpacyModelDescription model) {
		this.model = model;
	}

	@Override
	protected void save(ModelContentWO model) {
		this.model.save(model);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		this.model.load(model);
	}

	/**
	 * @return the model description.
	 */
	public SpacyModelDescription getModel() {
		return model;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Path: ").append(model.getPath()).append("\n");
		return sb.toString();
	}
}
