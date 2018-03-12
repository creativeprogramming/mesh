package com.gentics.mesh.util;

import static com.gentics.mesh.util.RxUtil.READ_ONLY;

import io.reactivex.Single;
import io.vertx.core.file.FileProps;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.file.AsyncFile;
import io.vertx.reactivex.core.file.FileSystem;

/**
 * An open file in read mode with its filesystem properties.
 */
public class PropReadFileStream {
	private FileProps props;
	private AsyncFile file;
	private String path;

	private PropReadFileStream(FileProps props, AsyncFile file, String path) {
		this.props = props;
		this.file = file;
		this.path = path;
	}

	/**
	 * Opens a file and reads its properties.
	 * 
	 * @param path
	 *            Path to the file
	 * @return
	 */
	public static Single<PropReadFileStream> openFile(Vertx vertx, String path) {
		FileSystem fs = vertx.fileSystem();
		return Single.zip(fs.rxProps(path).map(props -> props.getDelegate()), fs.rxOpen(path, READ_ONLY),
			(props, file) -> {
				return new PropReadFileStream(props, file, path);
			});
	}

	/**
	 * Gets the filesystem props
	 * 
	 * @return
	 */
	public FileProps getProps() {
		return props;
	}

	/**
	 * Gets the opened file, ready to read.
	 * 
	 * @return
	 */
	public AsyncFile getFile() {
		return file;
	}

	/**
	 * Return the file path.
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}
}
