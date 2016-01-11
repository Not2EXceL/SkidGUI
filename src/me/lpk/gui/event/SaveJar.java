package me.lpk.gui.event;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.remap.SkidMapper;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.MapTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class SaveJar implements EventHandler<ActionEvent> {
	private final MapTab tab;

	public SaveJar(MapTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			File jar = Main.getTargetJar();
			MappingGen.setLast(jar);
			Map<String, ClassNode> nodes = tab.getNodes();
			Map<String, MappedClass> renamed = tab.getRemap();
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Saving " + renamed.size() + " classes... ");
			int workIndex = 1;
			SkidMapper mapper = new SkidMapper(renamed);
			for (ClassNode cn : nodes.values()) {
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				ClassVisitor remapper = new RemappingClassAdapter(cw, mapper);
				cr.accept(remapper, ClassReader.EXPAND_FRAMES);
				cr = new ClassReader(cw.toByteArray());
				cw = new ClassWriter(0);
				cr.accept(cw, ClassReader.SKIP_DEBUG);
				out.put(renamed.get(cn.name).getRenamed(), cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (renamed.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + renamed.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			JarUtil.saveAsJar(out, jar.getName().replace(".jar", "") + "_Re.jar", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
