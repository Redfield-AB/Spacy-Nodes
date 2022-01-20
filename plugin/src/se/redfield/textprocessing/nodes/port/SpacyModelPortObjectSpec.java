/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class SpacyModelPortObjectSpec extends AbstractSimplePortObjectSpec {
	/**
	 * The serializer for the {@link SpacyModelPortObjectSpec}
	 *
	 */
	public static final class Serializer extends AbstractSimplePortObjectSpecSerializer<SpacyModelPortObjectSpec> {
	}

	private final SpacyModelDescription model;

	public SpacyModelPortObjectSpec() {
		this(new SpacyModelDescription());
	}

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
