package com.example.fooddex

object IconData {
    val iconList: List<Icon> = listOf(
        Icon(R.drawable.ic_grocery, "Prodotto"),
        Icon(R.drawable.ic_carrot, "Carota"),
        Icon(R.drawable.ic_chili, "Peperoncino"),
        Icon(R.drawable.ic_cucumber, "Cetriolo"),
        Icon(R.drawable.ic_eggplant, "Melanzana"),
        Icon(R.drawable.ic_garlic, "Aglio"),
        Icon(R.drawable.ic_ginger, "Zenzero"),
        Icon(R.drawable.ic_green_chili_pepper, "Peproncino Verde"),
        Icon(R.drawable.ic_pepper, "Peperone"),
        Icon(R.drawable.ic_tomato, "Pomodoro"),
        Icon(R.drawable.ic_lemon, "Limone"),
        Icon(R.drawable.ic_banana, "Banana"),
        Icon(R.drawable.ic_orange, "Arancia"),
        Icon(R.drawable.ic_potato, "Patata"),
        Icon(R.drawable.ic_lettuce, "Lattuga"),
        Icon(R.drawable.ic_kiwi, "Kiwi"),
        Icon(R.drawable.ic_pumpkin, "Zucca"),
        Icon(R.drawable.ic_apple, "Mela"),
        Icon(R.drawable.ic_apricot, "Albicocca"),
        Icon(R.drawable.ic_artichoke, "Carciofo"),
        Icon(R.drawable.ic_balsamic_vinegar, "Aceto"),
        Icon(R.drawable.ic_blueberry, "Mirtilli"),
        Icon(R.drawable.ic_bread, "Pane"),
        Icon(R.drawable.ic_butter, "Burro"),
        Icon(R.drawable.ic_canned_food, "Cibo in scatola"),
        Icon(R.drawable.ic_cereals, "Cereali"),
        Icon(R.drawable.ic_cheese, "Formaggio"),
        Icon(R.drawable.ic_cherry, "Ciliegie"),
        Icon(R.drawable.ic_chocolate, "Cioccolato"),
        Icon(R.drawable.ic_coffee_beans, "Caffè"),
        Icon(R.drawable.ic_cookies, "Biscotti"),
        Icon(R.drawable.ic_egg_carton, "Uova"),
        Icon(R.drawable.ic_fish, "Pesce"),
        Icon(R.drawable.ic_meat, "Carne"),
        Icon(R.drawable.ic_milk, "Latte"),
        Icon(R.drawable.ic_olive_oil, "Olio"),
        Icon(R.drawable.ic_rice, "Riso"),
        Icon(R.drawable.ic_salt, "Sale"),
        Icon(R.drawable.ic_soda_can, "Soda"),
        Icon(R.drawable.ic_sugar, "Zucchero"),
        Icon(R.drawable.ic_water, "Acqua"),
        Icon(R.drawable.ic_wheat, "Farina"),
        Icon(R.drawable.ic_wine, "Vino"),

    )
    fun sortIconList(iconList: List<Icon>): List<Icon> {
        // Sort the iconList based on icon name (excluding "Prodotto")
        val sortedIconList = iconList.filter { it.iconName != "Prodotto" }
            .sortedBy { it.iconName }
            .toMutableList()

        // Add the "Prodotto" icon as the first item in the sorted list
        val prodottoIcon = iconList.find { it.iconName == "Prodotto" }
        prodottoIcon?.let { sortedIconList.add(0, it) }

        return sortedIconList
    }

    val sortedIconList = sortIconList(iconList)
}