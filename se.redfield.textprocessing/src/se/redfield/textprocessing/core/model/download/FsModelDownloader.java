/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.filehandling.core.connections.FSFiles;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.meta.FSType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;

import se.redfield.textprocessing.core.model.SpacyFeature;
import se.redfield.textprocessing.core.model.SpacyModelDescription;
import se.redfield.textprocessing.core.model.SpacyModelMeta;
import se.redfield.textprocessing.prefs.SpacyPreferences;

/**
 * {@link SpacyModelDownloader} for the models provided by FS Path
 * 
 * @author Alexander Bondaletov
 *
 */
public class FsModelDownloader extends SpacyModelDownloader {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(FsModelDownloader.class);

	private final SettingsModelReaderFileChooser fsPath;
	private final boolean isLocal;

	/**
	 * @param fsPath The model directory path
	 */
	public FsModelDownloader(SettingsModelReaderFileChooser fsPath) {
		this.fsPath = fsPath;
		isLocal = fsPath.getLocation().getFSType().equals(FSType.LOCAL_FS);
	}

	@Override
	public SpacyModelDescription getModelDescription(boolean configure) throws InvalidSettingsException {
		if (configure) {
			return new SpacyModelDescription(fsPath.getLocation().getPath(), null);
		} else {
			return readFromMeta();
		}
	}

	private SpacyModelDescription readFromMeta() throws InvalidSettingsException {
		try (ReadPathAccessor accessor = fsPath.createReadPathAccessor()) {
			FSPath path = accessor.getRootPath(m -> {
			});

			FSPath metaFilePath = path.resolve(new String[] { "meta.json" });
			if (!FSFiles.exists(metaFilePath)) {
				throw new InvalidSettingsException("meta.json file is not found");
			}

			try (Reader reader = Files.newBufferedReader(metaFilePath)) {
				SpacyModelMeta meta = SpacyModelMeta.read(reader);
				return new SpacyModelDescription(getModelDownloadDir(accessor).toString(),
						SpacyFeature.fromPipeline(meta.getComponents()));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new InvalidSettingsException(e);
		}
	}

	@Override
	protected File getModelDownloadDir() throws InvalidSettingsException {
		try (ReadPathAccessor accessor = fsPath.createReadPathAccessor()) {
			return getModelDownloadDir(accessor);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	private File getModelDownloadDir(ReadPathAccessor accessor) throws IOException, InvalidSettingsException {
		FSPath path = accessor.getRootPath(m -> {
		});
		if (isLocal) {
			return new File(path.toAbsolutePath().toString());
		} else {
			return new File(getCacheDir(), path.getFileName().toString());
		}
	}

	private static File getCacheDir() {
		return new File(SpacyPreferences.getCacheDir(), "fs");
	}

	@Override
	protected void download(ExecutionMonitor exec)
			throws IOException, InvalidSettingsException, CanceledExecutionException {
		if (!isLocal) {
			try (ReadPathAccessor accessor = fsPath.createReadPathAccessor()) {
				FSPath path = accessor.getRootPath(m -> {
				});
				Path target = getModelDownloadDir(accessor).toPath();

				ComputeSizeVisitor sizeCalculator = new ComputeSizeVisitor();
				Files.walkFileTree(path, sizeCalculator);
				Files.walkFileTree(path, new CopyDirectoryVisitor(path, target, sizeCalculator.getSize(), exec));
				exec.checkCanceled();
			}
		}
	}

	private class ComputeSizeVisitor extends SimpleFileVisitor<Path> {

		private long size = 0;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			size += Files.size(file);
			return FileVisitResult.CONTINUE;
		}

		public long getSize() {
			return size;
		}
	}

	private class CopyDirectoryVisitor extends SimpleFileVisitor<Path> {

		private final ExecutionMonitor exec;
		private final Path source;
		private final Path target;
		private final long totalSize;
		private long done;

		public CopyDirectoryVisitor(Path source, Path target, long totalSize, ExecutionMonitor exec) {
			this.exec = exec;
			this.source = source;
			this.target = target;
			this.totalSize = totalSize;
			this.done = 0;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			Files.createDirectories(resolve(dir));
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.copy(file, resolve(file), StandardCopyOption.REPLACE_EXISTING);

			done += Files.size(file);
			exec.setProgress((double) done / totalSize);

			try {
				exec.checkCanceled();
				return FileVisitResult.CONTINUE;
			} catch (CanceledExecutionException e) {
				return FileVisitResult.TERMINATE;
			}
		}

		private Path resolve(Path p) {
			return target.resolve(source.relativize(p).toString());
		}
	}
}
