package me.lpk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import me.lpk.asm.FixableClassNode;
import me.lpk.mapping.MappingGen;

public class JarUtil {
	/**
	 * Creates a map of <String(Class name), ClassNode> for a given jar file
	 * 
	 * @param jarFile
	 * @author Konloch (Bytecode Viewer)
	 * @return
	 * @throws IOException
	 */
	public static Map<String, FixableClassNode> loadClasses(File jarFile) throws IOException {
		Map<String, FixableClassNode> classes = new HashMap<String, FixableClassNode>();
		ZipInputStream jis = new ZipInputStream(new FileInputStream(jarFile));
		ZipEntry entry;
		while ((entry = jis.getNextEntry()) != null) {
			try {
				final String name = entry.getName();
				if (name.endsWith(".class")) {
					byte[] bytes = IOUtils.toByteArray(jis);
					String cafebabe = String.format("%02X%02X%02X%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
					if (cafebabe.toLowerCase().equals("cafebabe")) {
						try {
							final FixableClassNode cn = ASMUtil.getNode(bytes);
							classes.put(cn.name, cn);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jis.closeEntry();
			}
		}
		jis.close();
		return classes;
	}

	/**
	 * Gets the manifest file of a given jar
	 * 
	 * @param jarFile
	 * @return
	 * @throws IOException
	 */
	public static String getManifest(File jarFile) throws IOException {
		URL url = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/META-INF/MANIFEST.MF");
		InputStream is = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = br.readLine()) != null) {
			sb.append(s + "\n");
		}
		br.close();
		is.close();
		String text = sb.toString();
		return text;
	}

	/**
	 * Saves a map of nodes to a jar file
	 * 
	 * @param nodes
	 * @param fileName
	 */
	public static void saveAsJar(Collection<FixableClassNode> nodes, String fileName) {
		try {
			JarOutputStream out = new JarOutputStream(new java.io.FileOutputStream(fileName));
			for (ClassNode cn : nodes) {
				ClassWriter cw = new ClassWriter(Opcodes.ASM5);
				cn.accept(cw);
				out.putNextEntry(new ZipEntry(cn.name + ".class"));
				out.write(cw.toByteArray());
				out.closeEntry();
			}
			// TODO: Detect if there is a META-INF and copy it if there is
			boolean hasMeta = true;
			if (hasMeta) {
				out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
				out.write(getManifestBytes(MappingGen.getLast()));
				out.closeEntry();
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the bytes of a manifest file within a jar file given in the
	 * parameter
	 * 
	 * @param jar
	 * @return
	 */
	private static byte[] getManifestBytes(File jar) {
		try {
			if (jar != null && jar.exists()) {
				StringBuilder sb = new StringBuilder(getManifest(jar));
				String strMain = "Main-Class: ";
				String strPath = "Class-Path: ";
				int mainIndex = sb.indexOf(strMain);
				int pathIndex = sb.indexOf(strPath);
				String main = sb.substring(mainIndex, mainIndex + strMain.length()) + MappingGen.getMain() + "\n\r";
				String path = sb.substring(pathIndex, mainIndex);
				sb.delete(0, sb.length());
				sb.append("Manifest-Version: 1.0\n");
				sb.append(path);
				sb.append(main);
				//UTF-8 required in case the main class contains unicode characters.
				return sb.toString().getBytes(Charset.forName("UTF-8"));
			}
			//TODO: Get a template / BS Manifest's bytes and make it the default return
			return new byte[] { 1, 2, 3, 4 };
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[] { 0 };
	}

}
