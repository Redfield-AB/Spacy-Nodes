/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

public class SpacyModelPortObject extends AbstractSimplePortObject {

	/**
	 * Serializer for the {@link SpacyModelPortObject}.
	 */
	public static final class Serializer extends AbstractSimplePortObjectSerializer<SpacyModelPortObject> {
	}

	/**
	 * The type of this port.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(SpacyModelPortObject.class);

	private SpacyModelPortObjectSpec spec;

	public SpacyModelPortObject() {
		this(null);
	}

	public SpacyModelPortObject(SpacyModelPortObjectSpec spec) {
		this.spec = spec;
	}

	@Override
	public String getSummary() {
		return spec.toString();
	}

	@Override
	public SpacyModelPortObjectSpec getSpec() {
		return spec;
	}

	@Override
	protected void save(ModelContentWO model, ExecutionMonitor exec) throws CanceledExecutionException {
		// nothing to save
	}

	@Override
	protected void load(ModelContentRO model, PortObjectSpec spec, ExecutionMonitor exec)
			throws InvalidSettingsException, CanceledExecutionException {
		this.spec = (SpacyModelPortObjectSpec) spec;
	}

	public String getModelPath() {
		return spec.getModel().getPath();
	}
}
