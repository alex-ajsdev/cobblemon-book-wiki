package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution;
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.evolution.variants.BlockClickEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.ItemInteractionEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.LevelUpEvolution;
import com.cobblemon.mod.common.pokemon.evolution.variants.TradeEvolution;
import com.cobblemon.mod.common.registry.BlockIdentifierCondition;
import com.cobblemon.mod.common.registry.BlockTagCondition;
import net.ajsdev.cobblemonbookwiki.book.WikiBookBuilder;
import net.ajsdev.cobblemonbookwiki.util.EvolutionRequirementUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvolutionPage {

    public static List<MutableComponent> build(FormData formData, RegistryAccess ra) {
        List<MutableComponent> components = new ArrayList<>();

        components.add(
                Component.translatable("cobblemon_book_wiki.evolutions.title")
                        .append("\n\n")
                        .withStyle(ChatFormatting.BOLD)
        );

        List<Evolution> evolutions = formData.getEvolutions().stream().toList();
        if (evolutions.isEmpty()) {
            components.add(
                    Component.translatable("cobblemon_book_wiki.evolutions.none")
            );
        }

        for (Evolution evolution : evolutions) {
            MutableComponent hover = Component.empty();

            switch (evolution) {
                case LevelUpEvolution ignored -> {
                    hover.append(
                            Component.translatable("cobblemon_book_wiki.evolution.type.level_up")
                    ).append("\n");
                }

                case TradeEvolution te -> {
                    hover.append(
                            Component.translatable("cobblemon_book_wiki.evolution.type.trade")
                    ).append("\n");

                    if (te.getRequiredContext().getSpecies() != null) {
                        Pokemon tradeForPokemon = te.getRequiredContext().create();
                        String tradeForName = WikiBookBuilder.getFullNameString(
                                tradeForPokemon.getForm(),
                                tradeForPokemon.getSpecies()
                        );

                        hover.append(
                                Component.translatable(
                                        "cobblemon_book_wiki.evolution.trade_for",
                                        tradeForName
                                )
                        ).append("\n");
                    }
                }

                case BlockClickEvolution bce -> {
                    hover.append(
                            Component.translatable("cobblemon_book_wiki.evolution.type.block_click")
                    ).append("\n");

                    RegistryLikeCondition<Block> cond = bce.getRequiredContext();
                    String blockString = "unknown";

                    if (cond instanceof BlockTagCondition btc)
                        blockString = btc.getTag().location().getPath();
                    if (cond instanceof BlockIdentifierCondition bic)
                        blockString = bic.getIdentifier().getPath();

                    hover.append(
                            Component.translatable(
                                    "cobblemon_book_wiki.evolution.interact_with",
                                    blockString
                            )
                    );
                }

                case ItemInteractionEvolution iie -> {
                    hover.append(
                            Component.translatable("cobblemon_book_wiki.evolution.type.use_item")
                    ).append("\n");

                    Optional<HolderSet<Item>> items = iie.getRequiredContext().items();
                    if (items.isPresent()) {
                        items.get().forEach(itemHolder -> {
                            Item item = itemHolder.value();
                            Component itemName = item.getName(new ItemStack(item));
                            hover.append(
                                    Component.translatable(
                                            "cobblemon_book_wiki.evolution.item",
                                            itemName
                                    )
                            ).append("\n");
                        });
                    } else {
                        hover.append(
                                Component.translatable(
                                        "cobblemon_book_wiki.evolution.item",
                                        Component.translatable("cobblemon_book_wiki.unknown")
                                )
                        ).append("\n");
                    }
                }

                default -> {
                    hover.append(
                            Component.translatable("cobblemon_book_wiki.evolution.type.unknown")
                    ).append("\n");
                }
            }

            hover.append(" \n");

            if (!evolution.getRequirements().isEmpty()) {
                hover.append(
                        Component.translatable("cobblemon_book_wiki.evolution.requirements")
                                .withStyle(ChatFormatting.BOLD)
                ).append("\n");

                for (Requirement req : evolution.getRequirements()) {
                    hover.append(
                            Component.literal(
                                    EvolutionRequirementUtil.getReadableString(req, ra)
                            )
                    ).append("\n");
                }
            }

            Pokemon evo = evolution.getResult().create();
            String evoName = WikiBookBuilder.getFullNameString(
                    evo.getForm(),
                    evo.getSpecies()
            );

            String name = evolution.getResult().getSpecies() == null
                    ? "Error"
                    : evoName;

            components.add(
                    Component.literal(name + "\n\n")
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
                            )
            );
        }

        return components;
    }
}
