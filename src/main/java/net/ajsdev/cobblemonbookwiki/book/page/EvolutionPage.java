package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.api.pokemon.evolution.requirement.EvolutionRequirement;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution;
import com.cobblemon.mod.common.registry.BlockIdentifierCondition;
import com.cobblemon.mod.common.registry.BlockTagCondition;
import com.cobblemon.mod.common.registry.ItemIdentifierCondition;
import com.cobblemon.mod.common.registry.ItemTagCondition;
import net.ajsdev.cobblemonbookwiki.book.WikiBookBuilder;
import net.ajsdev.cobblemonbookwiki.util.EvolutionRequirementUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class EvolutionPage {

    public static List<MutableComponent> build(FormData formData, RegistryAccess ra) {
        List<MutableComponent> components = new ArrayList<>();
        components.add(Component.literal("Evolutions: \n\n").withStyle(ChatFormatting.BOLD));

        List<Evolution> evolutions = formData.getEvolutions().stream().toList();
        if (evolutions.isEmpty()) {
            components.add(Component.literal("This pokemon does not evolve."));
        }


        for (Evolution evolution : evolutions) {
            MutableComponent hover = Component.empty();

            switch (evolution) {
                case LevelUpEvolution ignored: {
                    hover.append("Level Up Evolution\n");
                    break;
                }
                case TradeEvolution te: {
                    hover.append("Trade Evolution\n");
                    if (te.getRequiredContext().getSpecies() != null) {
                        Pokemon tradeForPokemon = te.getRequiredContext().create();
                        String tradeForName = WikiBookBuilder.getFullNameString(tradeForPokemon.getForm(),
                                tradeForPokemon.getSpecies());
                        hover.append(String.format("- Trade For: %s\n", tradeForName));
                    }
                    break;
                }
                case BlockClickEvolution bce: {
                    hover.append("Block Click Evolution\n");
                    RegistryLikeCondition<Block> cond = bce.getRequiredContext();
                    String blockString = "unknown";
                    if (cond instanceof BlockTagCondition btc)
                        blockString = btc.getTag().location().getPath();
                    if (cond instanceof BlockIdentifierCondition bic)
                        blockString = bic.getIdentifier().getPath();
                    MutableComponent bceComponent = Component.literal("- Interact With: " + blockString);
                    hover.append(bceComponent);
                    break;
                }

                case ItemInteractionEvolution iie: {
                    hover.append("Use Item Evolution\n");
                    RegistryLikeCondition<Item> cond = iie.getRequiredContext().getItem();
                    String itemString = "unknown";
                    if (cond instanceof ItemTagCondition itc)
                        itemString = itc.getTag().location().getPath();
                    if (cond instanceof ItemIdentifierCondition iic)
                        itemString = iic.getIdentifier().getPath();
                    MutableComponent iieComponent = Component.literal("- Item: " + itemString);
                    hover.append(iieComponent);
                    break;
                }
                default: {
                    hover.append("Unknown?\n");
                }
                ;
            }


            hover.append(" \n");
            if (!evolution.getRequirements().isEmpty()) {
                hover.append(Component.literal("Requirements: \n").withStyle(ChatFormatting.BOLD));

                for (EvolutionRequirement req : evolution.getRequirements()) {
                    hover.append(Component.literal(EvolutionRequirementUtil.getReadableString(req, ra)))
                            .append("\n");

                }
            }

            Pokemon evo = evolution.getResult().create();
            String evoName = WikiBookBuilder.getFullNameString(evo.getForm(), evo.getSpecies());
            String name = evolution.getResult().getSpecies() == null ?
                    "Error"
                    : evoName;

            components.add(
                    Component.literal(String.format("%s\n\n", name))
                            .withStyle(Style.EMPTY
                                    .applyFormats(ChatFormatting.BOLD, ChatFormatting.BLUE)
                                    .withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            hover
                                    ))
                                    .withClickEvent(new ClickEvent(
                                            ClickEvent.Action.RUN_COMMAND,
                                            "/wiki " +
                                                    evo.getSpecies().getName().toLowerCase() + " " +
                                                    evo.getForm().getName().toLowerCase()
                                    ))
                            ));
        }
        return components;
    }
}
