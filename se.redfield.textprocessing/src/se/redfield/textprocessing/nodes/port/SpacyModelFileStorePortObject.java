/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.nodes.port;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JComponent;

import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

/**
 * Filestore based PortObject for SpacyModels. Such a PortObject makes the model
 * part of the workflow and enables sharing the workflow in a partially executed
 * state or deploying the model to a remote executor via Integrated Deployment.
 * 
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class SpacyModelFileStorePortObject extends FileStorePortObject implements ISpacyModelPortObject {

	private final SpacyModelPortObjectSpec m_spec;
	
	/**
	 * Creates a new SpacyModelFileStorePortObject.
	 * 
	 * @param spec of the model
	 * @param fsFactory for creating the FileStore
	 * @return the port object containing the model
	 * @throws IOException if copying the model into the FileStore fails
	 */
	public static SpacyModelFileStorePortObject create(final SpacyModelPortObjectSpec spec,
			final FileStoreFactory fsFactory) throws IOException {
		var modelPath = Path.of(spec.getModel().getPath());
		var modelName = modelPath.getFileName().toString();
		var fileStore = fsFactory.createFileStore(modelName);
		copyModelIntoFileStore(modelPath, fileStore.getFile().toPath());
		return new SpacyModelFileStorePortObject(spec, fileStore);
	}

	private static void copyModelIntoFileStore(final Path modelPath, final Path fileStorePath) throws IOException {
		try (var fileStream = Files.walk(modelPath)) {
			fileStream.forEach(source -> {
				var destination = fileStorePath.resolve(modelPath.relativize(source));
				try {
					Files.copy(source, destination);
				} catch (IOException e) {
					throw new CopyException(e);
				}
			});
		} catch (CopyException ex) {
			throw ex.getCause();
		}
	}

	/**
	 * Constructor for creating new models.
	 * 
	 * @param spec of the model
	 * @param fileStore the model is stored in
	 */
	private SpacyModelFileStorePortObject(final SpacyModelPortObjectSpec spec, final FileStore fileStore) {
		super(List.of(fileStore));
		m_spec = spec;
	}
	
	private SpacyModelFileStorePortObject(SpacyModelPortObjectSpec spec) {
		super();
		m_spec = spec;
	}

	private static final class CopyException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		CopyException(final IOException cause) {
			super(cause);
		}

		@Override
		public synchronized IOException getCause() {
			return (IOException) super.getCause();
		}

	}
	
	@Override
	public String getModelPath() {
		return getFileStore(0).getFile().getAbsolutePath();
	}

	@Override
	public SpacyModelPortObjectSpec getSpec() {
		return m_spec;
	}

	@Override
	public JComponent[] getViews() {
		return null; // NOSONAR
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof SpacyModelFileStorePortObject) {
			var other = (SpacyModelFileStorePortObject)obj;
			return m_spec.equals(other.m_spec) && super.equals(obj);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/**
	 * Serializer for the SpacyModelFileStorePortObject.
	 * Doesn't actually serialize everything because all information is in the FileStore.
	 * 
	 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
	 */
	public static final class Serializer extends PortObjectSerializer<SpacyModelFileStorePortObject> {

		@Override
		public void savePortObject(SpacyModelFileStorePortObject portObject, PortObjectZipOutputStream out,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// nothing to save
		}

		@Override
		public SpacyModelFileStorePortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			return new SpacyModelFileStorePortObject((SpacyModelPortObjectSpec)spec);
		}
		
	}

}
