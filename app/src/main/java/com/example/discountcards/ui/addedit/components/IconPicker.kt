package com.example.discountcards.ui.addedit.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalPharmacy
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.R

data class IconCategoryOption(
    val name: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
)

val CATEGORY_ICON_OPTIONS = listOf(
    IconCategoryOption("shopping_bag", R.string.cat_store, Icons.Outlined.ShoppingBag),
    IconCategoryOption("shopping_cart", R.string.cat_grocery, Icons.Outlined.ShoppingCart),
    IconCategoryOption("local_pharmacy", R.string.cat_pharmacy, Icons.Outlined.LocalPharmacy),
    IconCategoryOption("checkroom", R.string.cat_fashion, Icons.Outlined.Checkroom),
    IconCategoryOption("restaurant", R.string.cat_dining, Icons.Outlined.Restaurant),
    IconCategoryOption("local_gas_station", R.string.cat_fuel, Icons.Outlined.LocalGasStation),
    IconCategoryOption("confirmation_number", R.string.cat_tickets, Icons.Outlined.ConfirmationNumber),
    IconCategoryOption("card_giftcard", R.string.cat_gift, Icons.Outlined.CardGiftcard),
    IconCategoryOption("fitness_center", R.string.cat_sports, Icons.Outlined.FitnessCenter),
    IconCategoryOption("credit_card", R.string.cat_other, Icons.Outlined.CreditCard)
)

fun getCategoryIconByName(name: String): ImageVector {
    return CATEGORY_ICON_OPTIONS.find { it.name.equals(name, ignoreCase = true) }?.icon
        ?: Icons.Outlined.ShoppingBag
}

@Composable
fun IconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.category_icon_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
        ) {
            items(CATEGORY_ICON_OPTIONS) { option ->
                val isSelected = option.name.equals(selectedIconName, ignoreCase = true)
                val label = stringResource(option.labelResId)

                FilterChip(
                    selected = isSelected,
                    onClick = { onIconSelected(option.name) },
                    label = { Text(text = label) },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = label,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
