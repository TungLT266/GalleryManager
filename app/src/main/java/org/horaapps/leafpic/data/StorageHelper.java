package org.horaapps.leafpic.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.orhanobut.hawk.Hawk;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.progress.ErrorCause;
import org.horaapps.leafpic.progress.ProgressException;
import org.horaapps.leafpic.util.ApplicationUtils;
import org.horaapps.leafpic.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashSet;

import static org.horaapps.leafpic.data.MediaHelper.scanFile;

public class StorageHelper {

	private static final String TAG = "StorageHelper";
	private static final String PRIMARY_VOLUME_NAME = "primary";


	private static boolean isWritable(@NonNull final File file) {
		boolean isExisting = file.exists();

		try {
			FileOutputStream output = new FileOutputStream(file, true);
			try {
				output.close();
			}
			catch (IOException e) {
				// do nothing.
			}
		}
		catch (java.io.FileNotFoundException e) {
			return false;
		}
		boolean result = file.canWrite();


		if (!isExisting) {

			file.delete();
		}

		return result;
	}



	public static boolean mkdir(Context context, @NonNull final File dir) {
		boolean success = dir.exists();

		if (!success) success = dir.mkdir();


		if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			DocumentFile document = getDocumentFile(context, dir, true, true);

			success = document != null && document.exists();
		}


		if (success) scanFile(context, new String[] { dir.getPath() });

		return success;
	}

	private static File getTargetFile(File source, File targetDir) {
		File file = new File(targetDir, source.getName());
		if (!source.getParentFile().equals(targetDir) && !file.exists())
			return file;


		return new File(targetDir, StringUtils.incrementFileNameSuffix(source.getName()));
	}

	public static Uri getUriForFile(Context context, File file) {
		return FileProvider.getUriForFile(context, ApplicationUtils.getPackageName() + ".provider", file);
	}

	public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
		InputStream inStream = null;
		OutputStream outStream = null;

		boolean success = false;
		File target = getTargetFile(source, targetDir);

		try {
			inStream = new FileInputStream(source);


			if (isWritable(target)) {

				FileChannel inChannel = new FileInputStream(source).getChannel();
				FileChannel outChannel = new FileOutputStream(target).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
				success = true;
				try { inChannel.close(); } catch (Exception ignored) { }
				try { outChannel.close(); } catch (Exception ignored) { }
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

					if (isFileOnSdCard(context, source)) {
						DocumentFile sourceDocument = getDocumentFile(context, source, false, false);
						if (sourceDocument != null) {
							inStream = context.getContentResolver().openInputStream(sourceDocument.getUri());
						}
					}

					DocumentFile targetDocument = getDocumentFile(context, target, false, false);
					if (targetDocument != null) {
						outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
					}
				}
				else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

					Uri uri = getUriFromFile(context,target.getAbsolutePath());
					if (uri != null) {
						outStream = context.getContentResolver().openOutputStream(uri);
					}
				}

				if (outStream != null) {

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = inStream.read(buffer)) != -1) outStream.write(buffer, 0, bytesRead);
					success = true;
				}

			}
		} catch (Exception e) {
			Log.e(TAG, "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
			return false;
		}
		finally {
			try { inStream.close(); } catch (Exception ignored) { }
			try { outStream.close(); } catch (Exception ignored) { }
		}

		if (success) scanFile(context, new String[] { target.getPath() });
		return success;
	}

	private static boolean isFileOnSdCard(Context context, File file) {
		String sdcardPath = getSdcardPath(context);
		return sdcardPath != null && file.getPath().startsWith(sdcardPath);
	}


	public static boolean moveFile(Context context, @NonNull final File source, @NonNull final File target) {

		boolean success = source.renameTo(target);

		if (!success) {
			success = copyFile(context, source, target);
			if (success) {
				try {
					deleteFile(context, source);
					success = true;
				} catch (ProgressException e) {
					success = false;
				}
			}
		}


		return success;
	}


	private static Uri getUriFromFile(Context context, final String path) {
		ContentResolver resolver = context.getContentResolver();

		Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
						new String[] {BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
						new String[] {path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
		if (filecursor == null) {
			return null;
		}
		filecursor.moveToFirst();

		if (filecursor.isAfterLast()) {
			filecursor.close();
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, path);
			return resolver.insert(MediaStore.Files.getContentUri("external"), values);
		}
		else {
			int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
			Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
							Integer.toString(imageId)).build();
			filecursor.close();
			return uri;
		}
	}



	public static boolean deleteFilesInFolder(Context context, @NonNull final File folder) {


		boolean totalSuccess = true;

		String[] children = folder.list();
		if (children != null) {
			for (String child : children) {
				File file = new File(folder, child);
				if (!file.isDirectory()) {
					try {
						deleteFile(context, file);
					} catch (ProgressException e) {
						Log.e(TAG, "Failed to delete file", e);
						totalSuccess = false;
					}
				}
			}
		}
		return totalSuccess;
	}




	public static void deleteFile(Context context, @NonNull final File file) throws ProgressException {
		ErrorCause error = new ErrorCause(file.getName());



		boolean success = false;

		try {
			success = file.delete();
		} catch (Exception e) {
			error.addCause(e.getLocalizedMessage());
		}


		if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			DocumentFile document = getDocumentFile(context, file, false, false);
			success = document != null && document.delete();
			error.addCause("Failed SAF");
		}


		if (!success && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			ContentResolver resolver = context.getContentResolver();

			try {
				Uri uri = getUriForFile(context, file);
				if (uri != null) {
					resolver.delete(uri, null, null);
				}
				success = !file.exists();
			}
			catch (Exception e) {
				error.addCause(String.format("Failed CP: %s", e.getLocalizedMessage()));
				Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
				success = false;
			}
		}

		if (success) scanFile(context, new String[]{file.getPath()});
		else throw new ProgressException(error);
	}

	public static HashSet<File> getStorageRoots(Context context) {
		HashSet<File> paths = new HashSet<File>();
		for (File file : context.getExternalFilesDirs("external")) {
			if (file != null) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
				else
					paths.add(new File(file.getAbsolutePath().substring(0, index)));
			}
		}
		return paths;
	}

	public static String getSdcardPath(Context context) {
		for(File file : context.getExternalFilesDirs("external")) {
			if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
				else
					return new File(file.getAbsolutePath().substring(0, index)).getPath();
			}
		}
		return null;
	}


	public static boolean rmdir(Context context, @NonNull final File file) {

		if(!file.exists() && !file.isDirectory()) return false;

		String[] fileList = file.list();

		if(fileList != null && fileList.length > 0)

			return false;


		if (file.delete()) return true;



		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			DocumentFile document = getDocumentFile(context, file, true, true);
			return document != null && document.delete();
		}


		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
			resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);


			resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
							new String[] {file.getAbsolutePath()});
		}

		return !file.exists();
	}


	private static DocumentFile getDocumentFile(Context context, @NonNull final File file, final boolean isDirectory, final boolean createDirectories) {

		Uri treeUri = getTreeUri(context);

		if (treeUri == null) return null;

		DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
		String sdcardPath = getSavedSdcardPath(context);
		String suffixPathPart = null;

		if (sdcardPath != null) {
			if((file.getPath().indexOf(sdcardPath)) != -1)
				suffixPathPart = file.getAbsolutePath().substring(sdcardPath.length());
		} else {
			HashSet<File> storageRoots = StorageHelper.getStorageRoots(context);
			for(File root : storageRoots) {
				if (root != null) {
					if ((file.getPath().indexOf(root.getPath())) != -1)
						suffixPathPart = file.getAbsolutePath().substring(file.getPath().length());
				}
			}
		}

		if (suffixPathPart == null) {
			Log.d(TAG, "unable to find the document file, filePath:"+ file.getPath()+ " root: " + ""+sdcardPath);
			return null;
		}

		if (suffixPathPart.startsWith(File.separator)) suffixPathPart = suffixPathPart.substring(1);

		String[] parts = suffixPathPart.split("/");

		for (int i = 0; i < parts.length; i++) { // 3 is the

			DocumentFile tmp = document.findFile(parts[i]);
			if (tmp != null)
				document = document.findFile(parts[i]);
			else {
				if (i < parts.length - 1) {
					if (createDirectories) document = document.createDirectory(parts[i]);
					else return null;
				}
				else if (isDirectory) document = document.createDirectory(parts[i]);
				else return document.createFile("image", parts[i]);
			}
		}

		return document;
	}


	private static Uri getTreeUri(Context context) {
		String uriString = Hawk.get(context.getString(R.string.preference_internal_uri_extsdcard_photos), null);

		if (uriString == null) return null;
		return Uri.parse(uriString);
	}


	public static void saveSdCardInfo(Context context, @Nullable final Uri uri) {
		Hawk.put(context.getString(R.string.preference_internal_uri_extsdcard_photos),
						uri == null ? null : uri.toString());
		Hawk.put("sd_card_path", StorageHelper.getSdcardPath(context));
	}

	private static String getSavedSdcardPath(Context context) {
		return Hawk.get("sd_card_path", null);
	}


	public static String getMediaPath(final Context context, final Uri uri)
	{

		if (DocumentsContract.isDocumentUri(context, uri)) {


			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}


			}

			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
								Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}

			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
								split[1]
				};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		else if ("downloads".equals(uri.getAuthority())) { //download for chrome-dev workaround
			String[] seg = uri.toString().split("/");
			final String id = seg[seg.length - 1];
			final Uri contentUri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

			return getDataColumn(context, contentUri, null, null);
		}

		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			try {

				String a = getDataColumn(context, uri, null, null);
				if (a != null) return a;
			} catch (Exception ignored) { }



			String[] split = uri.getPath().split("/");
			int z = -1, len = split.length;
			for (int i = 0; i < len; i++) {
				if (split[i].equals("external_files")) {
					z = i;
					break;
				}
			}

			if(z != -1) {
				StringBuilder partialPath = new StringBuilder();
				for (int i = z + 1; i < len; i++)
					partialPath.append(split[i]).append('/');

				String p = partialPath.toString();
				for (File file : StorageHelper.getStorageRoots(context)) {
					File f = new File(file, p);
					if (f.exists()) return f.getPath();
				}
			}
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}


	private static String getDataColumn(Context context, Uri uri, String selection,
										String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}



	private static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}


	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}


	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
