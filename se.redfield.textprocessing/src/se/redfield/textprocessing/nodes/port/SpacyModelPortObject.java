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

/**
 * The SpaCy model port object.
 * 
 * @author Alexander Bondaletov
 * @deprecated use {@link SpacyModelFileStorePortObject} instead
 */
@Deprecated
public class SpacyModelPortObject extends AbstractSimplePortObject implements ISpacyModelPortObject {

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

	/**
	 * Default constructor
	 */
	public SpacyModelPortObject() {
		this(null);
	}

	/**
	 * @param spec The port object spec.
	 */
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

	/**
	 * @return The local path the model is stored in.
	 */
	public String getModelPath() {
		return spec.getModel().getPath();
	}
}
