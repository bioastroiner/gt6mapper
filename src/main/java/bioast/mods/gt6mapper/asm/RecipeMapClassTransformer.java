package bioast.mods.gt6mapper.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class RecipeMapClassTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!name.startsWith("greg")) return basicClass;
		if (!name.equals("gregapi.recipes.Recipe$RecipeMap")) return basicClass;
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);
		for (MethodNode m : classNode.methods) {
			if (m.name.equals("findRecipe")
					&& m.desc.equals("(Lgregapi/random/IHasWorldAndCoords;Lgregapi/recipes/Recipe;ZJLnet/minecraft/item/ItemStack;[Lnet/minecraftforge/fluids/FluidStack;[Lnet/minecraft/item/ItemStack;)Lgregapi/recipes/Recipe;")
			) {
				AbstractInsnNode current;
				current = m.instructions.getFirst();
				while (current.getNext() != null && !(current.getNext() instanceof MethodInsnNode)) {
					current = current.getNext();
				}
				AbstractInsnNode hookCall = new MethodInsnNode(Opcodes.INVOKESTATIC, "bioast/mods/gt6mapper/hooks/GTHooks", "onFindRecipe",
						"(" +
								"Lgregapi/recipes/Recipe$RecipeMap;" +
								"Lgregapi/random/IHasWorldAndCoords;" +
								"Lgregapi/recipes/Recipe;" +
								"ZZ" +
								"J" +
								"Lnet/minecraft/item/ItemStack;" +
								"[Lnet/minecraftforge/fluids/FluidStack;" +
								"[Lnet/minecraft/item/ItemStack;" +
								")" +
								"Lgregapi/recipes/Recipe;");
				m.instructions.remove(current.getNext());
				m.instructions.insert(current, hookCall);
				ClassWriter rWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				classNode.accept(rWriter);
				return rWriter.toByteArray();
			}
		}
		return basicClass;
	}
}
