package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.CommonAbilityType;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class OverviewPage {

    public static MutableComponent build(FormData formData, Species species, String fullName) {
        MutableComponent page = Component.empty();

        page.append(Component.literal(String.format("─ %04d ─\n\n", species.getNationalPokedexNumber())).withStyle(ChatFormatting.BOLD));
        page.append(formatName(species, fullName));
        page.append(formatTypes(formData));
        page.append(formatBaseStats(formData));
        page.append(formatTraining(formData));
        page.append(formatBreeding(formData, species));
        page.append(formatAbilities(formData));

        return page;
    }

    private static MutableComponent formatName(Species species, String fullName) {
        MutableComponent name = Component.literal(String.format("%s\n", fullName)).withStyle(ChatFormatting.BOLD);
        if (!species.getImplemented()) name.setStyle(
                Style.EMPTY.applyFormats(ChatFormatting.RED, ChatFormatting.BOLD)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("cobblemon_book_wiki.not_implemented")
                        )));
        return name;
    }

    private static MutableComponent formatTypes(FormData formData) {
        MutableComponent types = Component.empty();
        boolean first = true;
        for (ElementalType type : formData.getTypes()) {
            if (first) first = false;
            else types.append(Component.literal(", "));
            types.append(type.getDisplayName().copy().withColor(type.getHue()));
        }
        types.append("\n\n");
        return types;
    }

    private static MutableComponent formatBaseStats(FormData formData) {
        Map<Stat, Integer> baseStats = formData.getBaseStats();
        MutableComponent hover = Component.translatable("cobblemon_book_wiki.base_stats.title").append("\n");

        hover.append(Component.translatable("cobblemon_book_wiki.stat.hp", baseStats.get(Stats.HP))).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.stat.attack", baseStats.get(Stats.ATTACK))).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.stat.defence", baseStats.get(Stats.DEFENCE))).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.stat.special_attack", baseStats.get(Stats.SPECIAL_ATTACK))).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.stat.special_defense", baseStats.get(Stats.SPECIAL_DEFENCE))).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.stat.speed", baseStats.get(Stats.SPEED))).append("\n");

        int total = baseStats.entrySet().stream()
                .filter(entry -> entry.getKey().getType() == Stat.Type.PERMANENT)
                .mapToInt(Map.Entry::getValue)
                .sum();

        hover.append("\n").append(Component.translatable("cobblemon_book_wiki.stat_total", total));

        return Component.translatable("cobblemon_book_wiki.base_stats.label")
                .append("\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.BLUE)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                );
    }

    private static MutableComponent formatTraining(FormData formData) {
        MutableComponent hover = Component.empty();

        // EV Yield
        Map<Stat, Integer> evYield = formData.getEvYield();
        hover.append(Component.translatable("cobblemon_book_wiki.ev_yield.title")).append("\n");

        int total = 0;
        for (Map.Entry<Stat, Integer> e : evYield.entrySet()) {
            Stat stat = e.getKey();
            int value = e.getValue();
            total += value;
            if (value > 0) {
                hover.append(Component.translatable("cobblemon_book_wiki.ev_yield.entry", value))
                        .append(stat.getDisplayName())
                        .append("\n");
            }
        }
        if (total == 0) hover.append(Component.translatable("cobblemon_book_wiki.none")).append("\n");
        hover.append(" \n");

        hover.append(Component.translatable("cobblemon_book_wiki.catch_rate", formData.getCatchRate())).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.base_friendship", formData.getBaseFriendship())).append("\n");
        hover.append(Component.translatable("cobblemon_book_wiki.base_exp", formData.getBaseExperienceYield())).append("\n");

        String growthRateRaw = formData.getExperienceGroup().getName(); // e.g. "MEDIUM_SLOW"
        String growthRatePretty = StringUtils.capitalize(growthRateRaw.replace("_", " "));
        // Mejorable: mapear a claves por enum; por ahora lo hacemos traducible con fallback "pretty"
        hover.append(Component.translatable("cobblemon_book_wiki.growth_rate", growthRatePretty));

        return Component.translatable("cobblemon_book_wiki.training_info.label")
                .append("\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                );
    }

    private static MutableComponent formatBreeding(FormData formData, Species species) {
        MutableComponent hover = Component.empty();

        hover.append(Component.translatable("cobblemon_book_wiki.egg_groups.title")).append("\n");
        for (EggGroup eggGroup : formData.getEggGroups()) {
            hover.append(Component.translatable("cobblemon_book_wiki.list_bullet"))
                    .append(Component.literal(eggGroup.getShowdownID()))
                    .append("\n");
        }

        hover.append(" \n");
        hover.append(formatGenderRatio(formData));

        int eggSteps = species.getEggCycles() * 257;
        hover.append(Component.translatable("cobblemon_book_wiki.egg_cycles", species.getEggCycles(), eggSteps));

        return Component.translatable("cobblemon_book_wiki.breeding_info.label")
                .append("\n")
                .withStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))
                );
    }

    private static Component formatGenderRatio(FormData formData) {
        MutableComponent genderRatio = Component.translatable("cobblemon_book_wiki.gender_ratio").append(" ");
        if (formData.getMaleRatio() == -1) {
            return genderRatio.append(Component.translatable("cobblemon_book_wiki.genderless").append("\n")
                    .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
        }

        int male = (int) (formData.getMaleRatio() * 100);
        int female = 100 - male;

        Component femaleText = Component.literal("♀ " + female + "%").withStyle(ChatFormatting.LIGHT_PURPLE);
        Component maleText = Component.literal("♂ " + male + "%").withStyle(ChatFormatting.BLUE);

        return genderRatio
                .append(femaleText)
                .append(Component.literal(" / "))
                .append(maleText)
                .append("\n");
    }

    private static Component formatAbilities(FormData formData) {
        MutableComponent abilities = Component.empty();
        abilities.append(Component.translatable("cobblemon_book_wiki.abilities").append("\n").withStyle(ChatFormatting.BOLD));

        List<PotentialAbility> potentialAbilities = formData.getAbilities().getMapping().values()
                .stream()
                .flatMap(List::stream)
                .toList();

        List<AbilityTemplate> commonAbilities = potentialAbilities
                .stream()
                .filter((potentialAbility -> potentialAbility.getType() instanceof CommonAbilityType))
                .map(PotentialAbility::getTemplate)
                .toList();

        List<String> commonAbilityNames = commonAbilities
                .stream()
                .map(AbilityTemplate::getName)
                .toList();

        List<AbilityTemplate> hiddenAbilities = potentialAbilities.stream()
                .filter((potentialAbility -> potentialAbility.getType() instanceof HiddenAbilityType))
                .map(PotentialAbility::getTemplate)
                .filter(potentialAbility -> !commonAbilityNames.contains(potentialAbility.getName()))
                .toList();

        for (AbilityTemplate template : commonAbilities) {
            Component name = Component.translatable(template.getDisplayName());
            Component description = Component.translatable(template.getDescription());

            Component entry = name.copy().withStyle(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
            );

            abilities.append(Component.translatable("cobblemon_book_wiki.list_bullet"))
                    .append(entry)
                    .append("\n");
        }

        for (AbilityTemplate template : hiddenAbilities) {
            Component name = Component.translatable(template.getDisplayName());
            Component description = Component.translatable(template.getDescription());

            Component entry = name.copy().withStyle(style ->
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, description))
            );

            abilities.append(Component.translatable("cobblemon_book_wiki.list_bullet"))
                    .append(Component.translatable("cobblemon_book_wiki.hidden_ability_prefix").withStyle(ChatFormatting.RED))
                    .append(entry)
                    .append("\n");
        }

        return abilities;
    }
}
