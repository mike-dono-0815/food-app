package com.guttracker.app.ui.icons

import com.guttracker.app.R

/** Bespoke icons exist only for the curated defaults. Anything else (custom/one-off items)
 *  falls back to a letter-monogram tile — see IconTile in ui/components. */
object ItemIcons {
    private val byName = mapOf(
        "Beer" to R.drawable.ic_beer,
        "Sparkling Wine" to R.drawable.ic_sparkling_wine,
        "Whiskey" to R.drawable.ic_whiskey,
        "Cocktail" to R.drawable.ic_cocktail,
        "Coffee" to R.drawable.ic_coffee,
        "Sausage" to R.drawable.ic_sausage,
        "Brettljause" to R.drawable.ic_brettljause,
        "Noodles" to R.drawable.ic_noodles,
        "Gulasch" to R.drawable.ic_gulasch,
        "Toast" to R.drawable.ic_toast,
        "Müsli" to R.drawable.ic_muesli,
        "Salad" to R.drawable.ic_salad,
        "Vegetarian" to R.drawable.ic_vegetarian,
        "Restaurant" to R.drawable.ic_restaurant,
    )

    fun iconFor(name: String): Int? = byName[name]

    fun monogramFor(name: String): String =
        name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            .ifEmpty { "?" }
}
