package me.lpk.gui.event;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.ClassNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.SkidMapper;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.ObfuscationTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.JarUtil;
import me.lpk.util.Timer;

public class Obfuscate implements EventHandler<ActionEvent> {
	private final ObfuscationTab tab;

	public Obfuscate(ObfuscationTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			Timer t = new Timer();
			File jar = Main.getTargetJar();
			MappingGen.setLast(jar);
			Map<String, ClassNode> nodes = JarUtil.loadClasses(jar);
			System.out.println("Generating new names for " + nodes.size() + " classes... ");
			Map<String, MappedClass> renamed = MappingGen.getRename(tab.getObfuscation(), nodes);
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Renaming " + renamed.size() + " classes... ");
			int workIndex = 1;
			SkidMapper mapper = new SkidMapper(renamed);
			for (ClassNode cn : nodes.values()) {
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				ClassVisitor remapper = new RemappingClassAdapter(cw, mapper);
				cn.accept(remapper);
				// out.put(cn.name, cw.toByteArray());
				out.put(renamed.get(cn.name).getRenamed(), cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (renamed.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + renamed.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			System.out.println("Saving classes...");
			JarUtil.saveAsJar(out, tab.getExportedName(), tab.forceMeta());
			System.out.println("Done! Completion time: " + (t.getTime()) + " Milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
