package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.*;
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.registry.*;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.cobblemon.mod.common.util.ResourceLocationExtensionsKt.asIdentifierDefaultingNamespace;
import static java.util.Objects.requireNonNullElse;

public class SpawnDetailPage {
    public static List<Component> build(FormData formData, Species species) {
        List<PokemonSpawnDetail> spawnDetails = getSpawnDetails(species, formData);
        List<Component> pages = new ArrayList<>(spawnDetails.size());
        AtomicInteger pageNo = new AtomicInteger(1);
        spawnDetails.forEach(sd -> handleSpawnDetail(sd, pages, pageNo.getAndIncrement(), spawnDetails.size()));
        return pages;
    }

    private static List<PokemonSpawnDetail> getSpawnDetails(Species species, FormData formData) {
        return CobblemonSpawnPools.WORLD_SPAWN_POOL.getDetails()
                .stream()
                .filter(detail -> detail instanceof PokemonSpawnDetail)
                .map(sd -> (PokemonSpawnDetail) sd)
                .filter(spawnDetail -> {
                    PokemonProperties pp = spawnDetail.getPokemon();
                    if (pp.getSpecies() == null) return false;
                    Species resolvedSpecies = PokemonSpecies.getByIdentifier(asIdentifierDefaultingNamespace(
                            pp.getSpecies(), Cobblemon.MODID));
                    if (species != resolvedSpecies) return false;
                    Pokemon pokemon = pp.create();
                    return pokemon.getSpecies().getName().equals(species.getName()) &&
                            pokemon.getForm().getName().equals(formData.getName());
                })
                .toList();
    }

    private static void handleSpawnDetail(PokemonSpawnDetail sd, List<Component> pages, int pageNo, int pageTotal) {
        MutableComponent page = Component.empty();

        page.append(Component.translatable("cobblemon_book_wiki.spawn_detail.title", pageNo, pageTotal)
                .withStyle(ChatFormatting.BOLD));

        Component weightComponent = Component.literal(String.format("(%s)\n", sd.getWeight()))
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("cobblemon_book_wiki.spawn_detail.weight")
                        ))
                );
        Component bucketComponent = Component.literal(String.format("%s ",
                StringUtils.capitalize(sd.getBucket().getName()))).append(weightComponent);
        page.append(bucketComponent);

        if (sd.getLevelRange() != null) {
            String levelRange = String.format("%s - %s", sd.getLevelRange().getFirst(), sd.getLevelRange().getEndInclusive());
            page.append(Component.translatable("cobblemon_book_wiki.spawn_detail.level_range", levelRange));
        }
        page.append(Component.literal(String.format("%s \n\n", StringUtils.capitalize(sd.getDisplayName()))));

        MutableComponent condHover = Component.empty();
        condHover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.conditions_title")).append("\n");
        if (sd.getConditions().isEmpty()) condHover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.none")).append("\n");
        else sd.getConditions().forEach(c -> handleCondition(condHover, c));

        page.append(
                Component.translatable("cobblemon_book_wiki.spawn_detail.conditions_button")
                        .withStyle(Style.EMPTY
                                .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        condHover
                                ))
                        ));

        MutableComponent antiCondHover = Component.empty();
        antiCondHover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.anticonditions_title")).append("\n");
        if (sd.getAnticonditions().isEmpty()) antiCondHover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.none")).append("\n");
        else sd.getAnticonditions().forEach(c -> handleCondition(antiCondHover, c));
        page.append(
                Component.translatable("cobblemon_book_wiki.spawn_detail.anticonditions_button")
                        .withStyle(Style.EMPTY
                                .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_RED)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        antiCondHover
                                ))
                        ));
        pages.add(page);

    }

    private static void handleCondition(MutableComponent hover, SpawningCondition<?> cond) {
        if (cond.isRaining() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.is_raining", yesNo(cond.isRaining()))).append("\n");
        if (cond.isThundering() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.is_thundering", yesNo(cond.isThundering()))).append("\n");
        if (cond.isSlimeChunk() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.slime_chunk", yesNo(cond.isSlimeChunk()))).append("\n");
        if (cond.getMoonPhase() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.moon_phase", cond.getMoonPhase())).append("\n");
        if (cond.getCanSeeSky() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.can_see_sky", yesNo(cond.getCanSeeSky()))).append("\n");
        if (cond.getMinY() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.min_y", cond.getMinY())).append("\n");
        if (cond.getMaxY() != null)
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.max_y", cond.getMaxY())).append("\n");

        if (cond.getMinLight() != null || cond.getMaxLight() != null) {
            int min = requireNonNullElse(cond.getMinLight(), 0);
            int max = requireNonNullElse(cond.getMaxLight(), 15);
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.light_level_range", min, max)).append("\n");
        }

        if (cond.getMinSkyLight() != null || cond.getMaxSkyLight() != null) {
            int min = requireNonNullElse(cond.getMinSkyLight(), 0);
            int max = requireNonNullElse(cond.getMaxSkyLight(), 15);
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.sky_light_level_range", min, max)).append("\n");
        }

        if (cond instanceof AreaSpawningCondition asCond) {
            if (asCond.getMinHeight() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.min_height", asCond.getMinHeight())).append("\n");
            if (asCond.getMaxHeight() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.max_height", asCond.getMaxHeight())).append("\n");
            if (asCond.getNeededNearbyBlocks() != null) {
                String blocks = asCond.getNeededNearbyBlocks().stream().map(rlc -> {
                    if (rlc instanceof BlockIdentifierCondition bic)
                        return bic.getIdentifier().getPath();
                    if (rlc instanceof BlockTagCondition btc)
                        return btc.getTag().location().getPath();
                    return "unknown";
                }).collect(Collectors.joining(", "));
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.nearby_blocks", blocks)).append("\n");
            }
        }

        if (cond instanceof FishingSpawningCondition fCond) {
            if (fCond.getMinLureLevel() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.min_lure_level", fCond.getMinLureLevel())).append("\n");
            if (fCond.getMaxLureLevel() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.max_lure_level", fCond.getMaxLureLevel())).append("\n");
            if (fCond.getBait() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.bait", fCond.getBait())).append("\n");
            if (fCond.getRodType() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.rod_type", fCond.getRodType())).append("\n");
        }

        if (cond instanceof GroundedSpawningCondition gCond) {
            if (gCond.getNeededBaseBlocks() != null) {
                String blocks = gCond.getNeededBaseBlocks().stream().map(rlc -> {
                    if (rlc instanceof BlockIdentifierCondition bic)
                        return bic.getIdentifier().getPath();
                    if (rlc instanceof BlockTagCondition btc)
                        return btc.getTag().location().getPath();
                    return "unknown";
                }).collect(Collectors.joining(", "));
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.base_blocks", blocks)).append("\n");
            }
        }

        if (cond instanceof SubmergedSpawningCondition sCond) {
            if (sCond.getMinDepth() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.min_depth", sCond.getMinDepth())).append("\n");
            if (sCond.getMaxDepth() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.max_depth", sCond.getMaxDepth())).append("\n");
            if (sCond.getFluidIsSource() != null)
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.needs_source_block", yesNo(sCond.getFluidIsSource()))).append("\n");
            if (sCond.getFluid() != null) {
                String fluidString = "Unknown";
                if (sCond.getFluid() instanceof FluidTagCondition ftc)
                    fluidString = ftc.getTag().location().getPath();
                if (sCond.getFluid() instanceof FluidIdentifierCondition fic)
                    fluidString = fic.getIdentifier().getPath();
                hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.fluid", fluidString)).append("\n");
            }
        }

        TimeRange timeRange = cond.getTimeRange();
        if (timeRange != null) {
            String timeName = "custom";
            for (Map.Entry<String, TimeRange> entry : TimeRange.Companion.getTimeRanges().entrySet()) {
                if (entry.getValue().getRanges().equals(timeRange.getRanges())) {
                    timeName = entry.getKey();
                    break;
                }
            }
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.time", timeName)).append("\n");
        }

        Set<RegistryLikeCondition<Biome>> biomeConds = cond.getBiomes();
        if (biomeConds != null && !biomeConds.isEmpty()) {
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.biomes_title")).append("\n");
            for (RegistryLikeCondition<Biome> rlc : biomeConds) {
                ResourceLocation resourceLocation = null;
                if (rlc instanceof BiomeTagCondition) {
                    resourceLocation = ((BiomeTagCondition) rlc).getTag().location();
                }
                if (rlc instanceof BiomeIdentifierCondition) {
                    resourceLocation = ((BiomeIdentifierCondition) rlc).getIdentifier();
                }

                if (resourceLocation != null) {
                    String pretty = Arrays.stream(resourceLocation.getPath().split("_"))
                            .map(s -> StringUtils.capitalize(s.toLowerCase()))
                            .collect(Collectors.joining(" "));

                    hover.append(Component.literal(String.format("- %s\n", pretty)));
                } else {
                    hover.append(Component.literal("- ¡Algo salió mal!"));
                }
            }
        }

        if (cond.getStructures() != null && !cond.getStructures().isEmpty()) {
            hover.append(Component.translatable("cobblemon_book_wiki.spawn_detail.structures_title")).append("\n");
            for (Either<ResourceLocation, TagKey<Structure>> either : cond.getStructures()) {
                ResourceLocation resourceLocation = null;

                if (either.left().isPresent())
                    resourceLocation = either.left().get();
                if (either.right().isPresent())
                    resourceLocation = either.right().get().location();

                if (resourceLocation != null) {
                    String pretty = Arrays.stream(resourceLocation.getPath().split("_"))
                            .map(s -> StringUtils.capitalize(s.toLowerCase()))
                            .collect(Collectors.joining(" "));

                    hover.append(Component.literal(String.format("- %s\n", pretty)));
                } else {
                    hover.append(Component.literal("- ¡Algo salió mal!"));
                }
            }
        }
    }

    private static String yesNo(Boolean value) {
        return value ? Component.translatable("cobblemon_book_wiki.common.yes").getString() :
                Component.translatable("cobblemon_book_wiki.common.no").getString();
    }
}
