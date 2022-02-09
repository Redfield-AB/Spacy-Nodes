/*
 * Copyright (c) 2021 Redfield AB.
*/
package se.redfield.textprocessing.core.model.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.connections.meta.FSType;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.ReadPathAccessor;
import org.knime.filehandling.core.defaultnodesettings.filechooser.reader.SettingsModelReaderFileChooser;

import se.redfield.textprocessing.prefs.SpacyPreferenceInitializer;

public class FsModelDownloader extends SpacyModelDownloader {
	private static final NodeLogger LOGGER = NodeLogger.getLogger(FsModelDownloader.class);

	private final SettingsModelReaderFileChooser fsPath;
	private final boolean isLocal;

	public FsModelDownloader(SettingsModelReaderFileChooser fsPath) {
		this.fsPath = fsPath;
		isLocal = fsPath.getLocation().getFSType().equals(FSType.LOCAL_FS);
	}

	@Override
	protected File getModelDownloadDir() throws InvalidSettingsException {
		try (ReadPathAccessor accessor = fsPath.createReadPathAccessor()) {
			FSPath path = accessor.getRootPath(m -> {
			});
			if (isLocal) {
				return new File(path.toAbsolutePath().toString());
			} else {
				return new File(getCacheDir(), path.getFileName().toString());
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	private static File getCacheDir() {
		return new File(SpacyPreferenceInitializer.getCacheDir(), "fs");
	}

	@Override
	protected void download(ExecutionMonitor exec) throws IOException, InvalidSettingsException {
		if (!isLocal) {
			try (ReadPathAccessor accessor = fsPath.createReadPathAccessor()) {
				FSPath path = accessor.getRootPath(m -> {
				});
				Path target = getModelDownloadDir().toPath();

				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						Files.createDirectories(resolve(dir));
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.copy(file, resolve(file), StandardCopyOption.REPLACE_EXISTING);
						return FileVisitResult.CONTINUE;
					}

					private Path resolve(Path p) {
						return target.resolve(path.relativize(p).toString());
					}
				});

			}
		}
	}

}
