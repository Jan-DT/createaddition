package com.mrh0.createaddition.recipe.charging;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class SequencedAssemblyChargingRecipeSerializer extends ProcessingRecipeSerializer<ChargingRecipe> {

    private static final String RESULT_KEY = "result";
    private static final String SEQUENCED_ASSEMBLY_RESULT_KEY = "results";
    private static final String INGREDIENT_KEY = "input";
    private static final String SEQUENCED_ASSEMBLY_INGREDIENT_KEY = "ingredients";

    public SequencedAssemblyChargingRecipeSerializer(ProcessingRecipeBuilder.ProcessingRecipeFactory<ChargingRecipe> factory) {
        super(factory);
    }

    @Override
    protected void writeToJson(JsonObject json, ChargingRecipe recipe) {
        super.writeToJson(json, recipe);
    }

    protected ItemStack readOutput(JsonObject json) {
        //note, the alternative keys are needed for kubejs create integration sequenced assembly recipe compat
        if (json.has(RESULT_KEY)) {
            var result = json.get(RESULT_KEY);
            if (result.isJsonObject() && result.getAsJsonObject().has("item")) {
                return ShapedRecipe.itemStackFromJson(result.getAsJsonObject());
            }
        } else if (json.has(SEQUENCED_ASSEMBLY_RESULT_KEY)) {
            var results = json.get(SEQUENCED_ASSEMBLY_RESULT_KEY);
            if (results.isJsonArray() && !results.getAsJsonArray().isEmpty()) {
                var result = results.getAsJsonArray().get(0).getAsJsonObject();
                if (!result.has("count")) {
                    result.add("count", new JsonPrimitive(1));
                }
                return ShapedRecipe.itemStackFromJson(result);
            }
        }
        return ItemStack.EMPTY;
    }

    protected Ingredient readIngredient(JsonObject json) {
        //note, the alternative keys are needed for kubejs create integration sequenced assembly recipe compat
        if (json.has(INGREDIENT_KEY)) {
            return Ingredient.fromJson(json.get(INGREDIENT_KEY).getAsJsonObject());
        } else if (json.has(SEQUENCED_ASSEMBLY_INGREDIENT_KEY)) {
            var jsonArray = json.get(SEQUENCED_ASSEMBLY_INGREDIENT_KEY);
            if (jsonArray.isJsonArray() && !jsonArray.getAsJsonArray().isEmpty()) {
                return Ingredient.fromJson(jsonArray.getAsJsonArray().get(0));
            }
        }
        return Ingredient.EMPTY;
    }


    @Override
    protected ChargingRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
        ItemStack output = readOutput(json);
        Ingredient input = readIngredient(json);
        int energy = json.get("energy").getAsInt();
        int maxChargeRate = Integer.MAX_VALUE;
        if(json.has("maxChargeRate")) maxChargeRate = json.get("maxChargeRate").getAsInt();
        return new ChargingRecipe(recipeId, input, output, energy, maxChargeRate);
    }

    @Override
    protected void writeToBuffer(FriendlyByteBuf buffer, ChargingRecipe recipe) {
        buffer.writeInt(recipe.maxChargeRate);
        recipe.ingredient.toNetwork(buffer);
        buffer.writeItem(recipe.output);
        buffer.writeInt(recipe.energy);
    }

    @Override
    protected ChargingRecipe readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int maxChargeRate = buffer.readInt();
        Ingredient input = Ingredient.fromNetwork(buffer);
        ItemStack output = buffer.readItem();
        int energy = buffer.readInt();
        return new ChargingRecipe(recipeId, input, output, energy, maxChargeRate);
    }
}
