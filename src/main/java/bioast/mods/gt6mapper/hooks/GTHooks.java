package bioast.mods.gt6mapper.hooks;

import cpw.mods.fml.common.eventhandler.Event;
import gregapi.random.IHasWorldAndCoords;
import gregapi.recipes.Recipe;
import gregapi.recipes.maps.RecipeMapPrinter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GTHooks {
	public static Recipe onFindRecipe(Recipe.RecipeMap aMap, IHasWorldAndCoords aTileEntity, Recipe aRecipe,/*to make ASM happy & change less code as possible*/boolean aLoop, boolean aNotUnificated, long aSize, ItemStack aSpecialSlot, FluidStack[] aFluids, ItemStack... aInputs) {
		Recipe rRecipe = aMap.findRecipeInternal(aTileEntity, aRecipe, aLoop, aNotUnificated, aSize, aSpecialSlot, aFluids, aInputs);
		OnFindGTRecipeEvent event = new OnFindGTRecipeEvent(aMap, aRecipe, aNotUnificated, aTileEntity, aSize, aSpecialSlot, aInputs, aFluids);
		MinecraftForge.EVENT_BUS.post(event)
		if (aRecipe == null && event.aRecipeConsumer.get() != null)
			return event.aRecipeConsumer.get();
		return rRecipe;
	}

	/**
	 * Used to generate dynamic recipes for Recipes such as Printers & Scanners
	 * This recipes are checked when a machine detects items in its inventory
	 * Here you tell it to run a recipe directly without registering
	 * of course it means this recipes are not seen in NEI so you need to add a Fake one for NEI
	 * It also won't let you override if a preDefined recipe is already found
	 *
	 * @author BioAstroiner
	 * @see cpw.mods.fml.common.eventhandler.Event
	 * @see RecipeMapPrinter
	 * <p>
	 * Set your Recipe Return value to this event's accept function
	 */
	public static class OnFindGTRecipeEvent extends Event implements Consumer<Recipe> {
		public Recipe.RecipeMap aRecipeMap;
		public Recipe aRecipe;
		public boolean aNotUnified;
		public IHasWorldAndCoords aTileEntity;
		public long aSize;
		public ItemStack aSpecial;
		public ItemStack[] aInputs;
		public FluidStack[] aFluids;

		Supplier<Recipe> aRecipeConsumer;

		public OnFindGTRecipeEvent(Recipe.RecipeMap aRecipeMap, Recipe aRecipe, boolean aNotUnified, IHasWorldAndCoords aTileEntity, long aSize, ItemStack aSpecial, ItemStack[] aInputs, FluidStack[] aFluids) {
			this.aRecipeMap = aRecipeMap;
			this.aRecipe = aRecipe;
			this.aNotUnified = aNotUnified;
			this.aTileEntity = aTileEntity;
			this.aSize = aSize;
			this.aSpecial = aSpecial;
			this.aInputs = aInputs;
			this.aFluids = aFluids;
		}

		@Override
		// return recipe here
		public void accept(Recipe recipe) {
			aRecipeConsumer = () -> recipe;
		}
	}
}
