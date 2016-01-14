package me.lpk.gui.event.patch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.zkm.ZKMClassVisitor;
import me.lpk.gui.Main;
import me.lpk.mapping.MappingGen;
import me.lpk.util.JarUtil;

public class PatchZKM implements EventHandler<ActionEvent> {

	@Override
	public void handle(ActionEvent ae) {
		try {
			File jar = Main.getTargetJar();
			Map<String, ClassNode> nodes = JarUtil.loadClasses(jar);
			MappingGen.setLast(jar);
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Saving " + nodes.size() + " classes... ");
			int workIndex = 1;
			for (ClassNode cn : nodes.values()) {
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				cn.accept(new ZKMClassVisitor(cw, cn));
				out.put(cn.name, cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (nodes.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + nodes.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			JarUtil.saveAsJar(out, jar.getName().replace(".jar", "") + "_Re_ZKM.jar", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
