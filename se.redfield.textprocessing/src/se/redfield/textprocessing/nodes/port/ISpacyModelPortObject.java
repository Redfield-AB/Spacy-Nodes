/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * Common interface of PortObjects holding spaCy models.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public interface ISpacyModelPortObject extends PortObject {

	/**
	 * The common PortType of implementing PortObjects.
	 */
	public static PortType TYPE = PortTypeRegistry.getInstance().getPortType(ISpacyModelPortObject.class);

	@Override
	SpacyModelPortObjectSpec getSpec();

	@Override
	default String getSummary() {
		return getSpec().toString();
	}

	/**
	 * @return the local path to the model
	 */
	String getModelPath();

	/**
	 * Serializer of ISpacyModelPortObject. All methods raise an
	 * IllegalStateException because @link ISpacyModelPortObject is an interface and
	 * only implementing classes should ever be serialized.
	 */
	public static final class Serializer extends PortObjectSerializer<ISpacyModelPortObject> {

		@Override
		public void savePortObject(final ISpacyModelPortObject portObject, final PortObjectZipOutputStream out,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			throw new IllegalStateException(
					"Serializing ISpacyModelPortObject is unsupported. Implement a serializer for the implementing type.");
		}

		@Override
		public ISpacyModelPortObject loadPortObject(final PortObjectZipInputStream in, final PortObjectSpec spec,
				final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			throw new IllegalStateException(
					"Deserializing ISpacyModelPortObject is unsupported. Implement a serializer for the implementing type.");
		}
	}

}
